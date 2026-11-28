package com.example.vetclinic.medical.treatment;

import com.example.vetclinic.audit.AuditService;
import com.example.vetclinic.clientsandpatients.Animal;
import com.example.vetclinic.clientsandpatients.AnimalRepository;
import com.example.vetclinic.clientsandpatients.Client;
import com.example.vetclinic.clientsandpatients.Species;
import com.example.vetclinic.common.exception.NotFoundException;
import com.example.vetclinic.franchise.Employee;
import com.example.vetclinic.franchise.EmployeeRepository;
import com.example.vetclinic.franchise.Location;
import com.example.vetclinic.franchise.Receptionist;
import com.example.vetclinic.franchise.VetGeneral;
import com.example.vetclinic.franchise.VetSurgeon;
import com.example.vetclinic.medical.appointment.Appointment;
import com.example.vetclinic.medical.appointment.AppointmentService;
import com.example.vetclinic.medical.appointment.AppointmentType;
import com.example.vetclinic.medical.treatment.exception.ConsentRequiredException;
import com.example.vetclinic.medical.treatment.exception.InvalidSurgeryStateException;
import com.example.vetclinic.medical.treatment.exception.UnauthorizedActionException;
import com.example.vetclinic.medical.vaccination.VaccinationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceTest {

    private static final LocalDate VISIT_DATE = LocalDate.of(2026, 12, 1);
    private static final LocalDateTime FOLLOW_UP_START = LocalDateTime.of(2026, 12, 8, 10, 0);
    private static final BigDecimal ESTIMATE = new BigDecimal("300.00");
    private static final BigDecimal FINAL_COST = new BigDecimal("340.00");
    private static final Long RECORD_ID = 10L;
    private static final Long SURGERY_ID = 20L;
    private static final Long PRODUCT_ID = 5L;

    @Mock private MedicalRecordRepository medicalRecordRepository;
    @Mock private TreatmentRepository treatmentRepository;
    @Mock private SurgeryRepository surgeryRepository;
    @Mock private AnimalRepository animalRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private AppointmentService appointmentService;
    @Mock private VaccinationService vaccinationService;
    @Mock private StockService stockService;
    @Mock private AuditService auditService;

    private TreatmentService treatmentService;

    private Location mainClinic;
    private Client owner;
    private Animal rex;
    private VetGeneral generalVet;
    private VetSurgeon surgeon;
    private Receptionist receptionist;
    private MedicalRecord record;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-12-01T09:00:00Z"), ZoneOffset.UTC);
        treatmentService = new TreatmentService(medicalRecordRepository, treatmentRepository,
                surgeryRepository, animalRepository, employeeRepository, appointmentService,
                vaccinationService, stockService, auditService, fixedClock);

        mainClinic = new Location(1L, "Main clinic", "1 High St");
        owner = new Client("OWN-0001", "Jane Doe", "jane@example.com", "600000000");
        rex = new Animal("PET-0001", "Rex", Species.DOG, owner);

        generalVet = new VetGeneral(1L, "Dr Smith", "smith@clinic.com");
        surgeon = new VetSurgeon(2L, "Dr Wilson", "wilson@clinic.com");
        receptionist = new Receptionist(3L, "Ana Lopez", "ana@clinic.com");
        generalVet.assignTo(mainClinic);
        surgeon.assignTo(mainClinic);
        receptionist.assignTo(mainClinic);

        record = new MedicalRecord(rex, null, VISIT_DATE, new BigDecimal("12.5"), "Limping", "Left hind leg");
        ReflectionTestUtils.setField(record, "id", RECORD_ID);
    }

    private void givenEmployee(Employee employee) {
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
    }

    private void givenRecord() {
        when(medicalRecordRepository.findById(RECORD_ID)).thenReturn(Optional.of(record));
    }

    /** A planned surgery on Rex, already saved with id 20 and attached to the record. */
    private Surgery plannedSurgery() {
        Surgery surgery = new Surgery(VISIT_DATE, ESTIMATE, surgeon, "Knee ligament repair");
        record.addTreatment(surgery);
        ReflectionTestUtils.setField(surgery, "id", SURGERY_ID);
        when(surgeryRepository.findById(SURGERY_ID)).thenReturn(Optional.of(surgery));
        return surgery;
    }

    // ------------------------------------------------------------ surgery

    @Test
    void planSurgery_byGeneralVet_isRejectedAsForbidden() {
        givenEmployee(generalVet);
        givenRecord();

        UnauthorizedActionException e = assertThrows(UnauthorizedActionException.class,
                () -> treatmentService.planSurgery(generalVet.getId(), RECORD_ID, "Knee ligament repair", ESTIMATE));

        assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(surgeryRepository, never()).save(any());
    }

    @Test
    void completeSurgery_withoutConsent_isRejected() {
        givenEmployee(surgeon);
        Surgery surgery = plannedSurgery();

        assertThrows(ConsentRequiredException.class, () -> treatmentService.completeSurgery(
                surgeon.getId(), SURGERY_ID, FINAL_COST, FOLLOW_UP_START, 30));

        assertThat(surgery.getStatus()).isEqualTo(SurgeryStatus.PLANNED);
        verifyNoInteractions(appointmentService, auditService);
    }

    @Test
    void completeSurgery_withConsent_booksFollowUpAndAuditsTheChanges() {
        givenEmployee(surgeon);
        Surgery surgery = plannedSurgery();
        surgery.attachConsent(new ConsentForm(owner, true, LocalDateTime.of(2026, 11, 30, 18, 0)));
        Appointment followUp = new Appointment(rex, surgeon, mainClinic, FOLLOW_UP_START, 30, AppointmentType.FOLLOW_UP);
        when(appointmentService.book("PET-0001", surgeon.getId(), mainClinic.getId(),
                FOLLOW_UP_START, 30, AppointmentType.FOLLOW_UP)).thenReturn(followUp);
        when(surgeryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Surgery completed = treatmentService.completeSurgery(
                surgeon.getId(), SURGERY_ID, FINAL_COST, FOLLOW_UP_START, 30);

        assertThat(completed.getStatus()).isEqualTo(SurgeryStatus.COMPLETED);
        assertThat(completed.getFollowUp()).isSameAs(followUp);
        assertThat(completed.getCost()).isEqualByComparingTo(FINAL_COST);
        assertThat(completed.getEstimatedCost()).isEqualByComparingTo(ESTIMATE);
        verify(auditService).record(surgeon, "Surgery", SURGERY_ID, "cost", ESTIMATE, FINAL_COST);
        verify(auditService).record(surgeon, "Surgery", SURGERY_ID, "status",
                SurgeryStatus.PLANNED, SurgeryStatus.COMPLETED);
    }

    @Test
    void completeSurgery_thatWasCancelled_isRejectedAsConflict() {
        givenEmployee(surgeon);
        Surgery surgery = plannedSurgery();
        surgery.cancel();

        InvalidSurgeryStateException e = assertThrows(InvalidSurgeryStateException.class,
                () -> treatmentService.completeSurgery(surgeon.getId(), SURGERY_ID, FINAL_COST, FOLLOW_UP_START, 30));

        assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verifyNoInteractions(appointmentService);
    }

    // --------------------------------------------------------- medication

    @Test
    void recordMedication_calculatesDoseFromRecordedWeight() {
        givenEmployee(generalVet);
        givenRecord();
        when(stockService.requiresPrescription(PRODUCT_ID)).thenReturn(true);
        when(treatmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Medication medication = treatmentService.recordMedication(generalVet.getId(), RECORD_ID,
                "Meloxicam", new BigDecimal("2.5"), 5, PRODUCT_ID, new BigDecimal("18.00"));

        // 2.5 mg/kg x 12.5 kg = 31.25 mg
        assertEquals(new BigDecimal("31.25"), medication.getDoseMg());
        assertThat(record.getTreatments()).containsExactly(medication);
        verify(stockService).consume(PRODUCT_ID, mainClinic.getId(), 1);
    }

    @Test
    void recordMedication_prescriptionProductByReceptionist_isRejected() {
        givenEmployee(receptionist);
        givenRecord();
        when(stockService.requiresPrescription(PRODUCT_ID)).thenReturn(true);

        assertThrows(UnauthorizedActionException.class, () -> treatmentService.recordMedication(
                receptionist.getId(), RECORD_ID, "Meloxicam", new BigDecimal("2.5"), 5, PRODUCT_ID, new BigDecimal("18.00")));

        verify(stockService, never()).consume(anyLong(), anyLong(), anyInt());
        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void recordMedication_overTheCounterProductByReceptionist_isAllowed() {
        givenEmployee(receptionist);
        givenRecord();
        when(stockService.requiresPrescription(PRODUCT_ID)).thenReturn(false);
        when(treatmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Medication medication = treatmentService.recordMedication(receptionist.getId(), RECORD_ID,
                "Flea shampoo", new BigDecimal("1"), 1, PRODUCT_ID, new BigDecimal("9.50"));

        assertThat(medication.getPerformedBy()).isSameAs(receptionist);
        verify(stockService).consume(PRODUCT_ID, mainClinic.getId(), 1);
    }

    // ----------------------------------------------------------- vaccines

    @Test
    void recordVaccine_movesTheVaccinationScheduleForward() {
        givenEmployee(generalVet);
        givenRecord();
        when(treatmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Vaccine vaccine = treatmentService.recordVaccine(generalVet.getId(), RECORD_ID,
                "Rabies", 2, 7L, null, new BigDecimal("35.00"));

        assertThat(vaccine.getDescription()).isEqualTo("Rabies vaccine (dose 2)");
        verify(vaccinationService).recordDose(7L, VISIT_DATE);
        verifyNoInteractions(stockService);
    }

    // -------------------------------------------------------- audit, errors

    @Test
    void changeCost_writesAnAuditEntryWithOldAndNewValue() {
        givenEmployee(generalVet);
        Vaccine vaccine = new Vaccine(VISIT_DATE, new BigDecimal("35.00"), generalVet, "Rabies", 1, null, null);
        ReflectionTestUtils.setField(vaccine, "id", 30L);
        when(treatmentRepository.findById(30L)).thenReturn(Optional.of(vaccine));
        when(treatmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        BigDecimal oldCost = vaccine.getCost();
        BigDecimal newCost = new BigDecimal("30.00");

        treatmentService.changeCost(generalVet.getId(), 30L, newCost);

        verify(auditService).record(eq(generalVet), eq("Vaccine"), eq(30L), eq("cost"), eq(oldCost), eq(newCost));
        assertThat(vaccine.getCost()).isEqualByComparingTo("30.00");
    }

    @Test
    void unknownEmployee_isNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> treatmentService.planSurgery(99L, RECORD_ID, "Knee ligament repair", ESTIMATE));

        assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        verifyNoInteractions(medicalRecordRepository, surgeryRepository);
    }
}
