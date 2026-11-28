package com.example.vetclinic.medical.treatment;

import com.example.vetclinic.audit.AuditService;
import com.example.vetclinic.clientsandpatients.Animal;
import com.example.vetclinic.clientsandpatients.AnimalRepository;
import com.example.vetclinic.common.exception.BusinessRuleException;
import com.example.vetclinic.common.exception.NotFoundException;
import com.example.vetclinic.franchise.Employee;
import com.example.vetclinic.franchise.EmployeeRepository;
import com.example.vetclinic.franchise.Location;
import com.example.vetclinic.franchise.StaffRole;
import com.example.vetclinic.medical.appointment.Appointment;
import com.example.vetclinic.medical.appointment.AppointmentService;
import com.example.vetclinic.medical.appointment.AppointmentType;
import com.example.vetclinic.medical.treatment.exception.ConsentRequiredException;
import com.example.vetclinic.medical.treatment.exception.InvalidSurgeryStateException;
import com.example.vetclinic.medical.treatment.exception.MedicalRecordNotFoundException;
import com.example.vetclinic.medical.treatment.exception.TreatmentNotFoundException;
import com.example.vetclinic.medical.treatment.exception.UnauthorizedActionException;
import com.example.vetclinic.medical.vaccination.VaccinationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * All the rules for medical records and treatments. Every method receives the id of
 * the employee doing the action, so the role checks live here (and are unit tested
 * here) rather than only in the security layer.
 *
 * The class is @Transactional: if any step fails (for example the follow-up
 * appointment clashes), nothing from that call is saved.
 */
@Service
@Transactional
public class TreatmentService {

    private static final int VACCINE_UNITS_PER_DOSE = 1;
    private static final int MEDICATION_UNITS_PER_PRESCRIPTION = 1;

    private final MedicalRecordRepository medicalRecordRepository;
    private final TreatmentRepository treatmentRepository;
    private final SurgeryRepository surgeryRepository;
    private final AnimalRepository animalRepository;
    private final EmployeeRepository employeeRepository;
    private final AppointmentService appointmentService;
    private final VaccinationService vaccinationService;
    private final StockService stockService;
    private final AuditService auditService;
    private final Clock clock;

    public TreatmentService(MedicalRecordRepository medicalRecordRepository,
                            TreatmentRepository treatmentRepository,
                            SurgeryRepository surgeryRepository,
                            AnimalRepository animalRepository,
                            EmployeeRepository employeeRepository,
                            AppointmentService appointmentService,
                            VaccinationService vaccinationService,
                            StockService stockService,
                            AuditService auditService,
                            Clock clock) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.treatmentRepository = treatmentRepository;
        this.surgeryRepository = surgeryRepository;
        this.animalRepository = animalRepository;
        this.employeeRepository = employeeRepository;
        this.appointmentService = appointmentService;
        this.vaccinationService = vaccinationService;
        this.stockService = stockService;
        this.auditService = auditService;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- records

    /** Opens the record for a visit. Only vets can write clinical records. */
    public MedicalRecord openRecord(Long actorId, String animalId, String appointmentReference,
                                    BigDecimal weightKg, String diagnosis, String notes) {
        Employee actor = findEmployee(actorId);
        if (!actor.canTreatAnimals()) {
            throw new UnauthorizedActionException(
                    actor.getName() + " (" + actor.getRole() + ") cannot write medical records");
        }
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new NotFoundException("Animal not found: " + animalId));
        Appointment appointment = appointmentReference == null
                ? null
                : appointmentService.getByReference(appointmentReference);

        MedicalRecord record = new MedicalRecord(animal, appointment, LocalDate.now(clock),
                weightKg, diagnosis, notes);
        return medicalRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public MedicalRecord getRecord(Long recordId) {
        return medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new MedicalRecordNotFoundException(recordId));
    }

    /** The animal's medical history: every record, oldest first. */
    @Transactional(readOnly = true)
    public List<MedicalRecord> getHistory(String animalId) {
        return medicalRecordRepository.findByAnimalIdOrderByDateAsc(animalId);
    }

    // --------------------------------------------------------------- vaccines

    /**
     * Gives a vaccine dose: checks the role, takes the dose from stock and moves the
     * animal's vaccination schedule forward (Person 2's VaccinationService).
     */
    public Vaccine recordVaccine(Long actorId, Long recordId, String vaccineName, int doseNumber,
                                 Long vaccinationScheduleId, Long productId, BigDecimal cost) {
        Employee actor = findEmployee(actorId);
        MedicalRecord record = getRecord(recordId);
        Vaccine vaccine = new Vaccine(record.getDate(), cost, actor, vaccineName,
                doseNumber, vaccinationScheduleId, productId);
        requireAllowed(actor, vaccine);

        if (productId != null) {
            stockService.consume(productId, locationIdOf(actor, record), VACCINE_UNITS_PER_DOSE);
        }
        if (vaccinationScheduleId != null) {
            vaccinationService.recordDose(vaccinationScheduleId, record.getDate());
        }
        record.addTreatment(vaccine);
        return treatmentRepository.save(vaccine);
    }

    // ------------------------------------------------------------- medication

    /**
     * Gives out a medicine. The dose is calculated from the weight in the record.
     * Prescription-only products need a vet; over-the-counter ones can also be
     * given out by a receptionist.
     */
    public Medication recordMedication(Long actorId, Long recordId, String medicineName,
                                       BigDecimal dosePerKgMg, int durationDays,
                                       Long productId, BigDecimal cost) {
        Employee actor = findEmployee(actorId);
        MedicalRecord record = getRecord(recordId);

        boolean prescriptionOnly = productId != null && stockService.requiresPrescription(productId);
        if (prescriptionOnly && !actor.canDispensePrescriptionProducts()) {
            throw new UnauthorizedActionException(
                    actor.getName() + " (" + actor.getRole() + ") cannot give out the prescription-only product "
                            + productId + "; a vet must do it");
        }

        Medication medication = new Medication(record.getDate(), cost, actor, medicineName,
                dosePerKgMg, record.getWeightKg(), durationDays, productId);
        if (productId != null) {
            stockService.consume(productId, locationIdOf(actor, record), MEDICATION_UNITS_PER_PRESCRIPTION);
        }
        record.addTreatment(medication);
        return treatmentRepository.save(medication);
    }

    // ---------------------------------------------------------------- surgery

    /** Plans a surgery. Only a VetSurgeon can be the one who performs it. */
    public Surgery planSurgery(Long surgeonId, Long recordId, String procedure, BigDecimal estimatedCost) {
        Employee surgeon = findEmployee(surgeonId);
        MedicalRecord record = getRecord(recordId);
        Surgery surgery = new Surgery(record.getDate(), estimatedCost, surgeon, procedure);
        requireAllowed(surgeon, surgery);

        record.addTreatment(surgery);
        return surgeryRepository.save(surgery);
    }

    /** Records the owner's answer. The owner is taken from the animal, not passed in. */
    public Surgery signConsent(Long surgeryId, boolean accepted) {
        Surgery surgery = findSurgery(surgeryId);
        requireStatus(surgery, SurgeryStatus.PLANNED, "sign consent for");
        ConsentForm form = new ConsentForm(surgery.getMedicalRecord().getAnimal().getOwner(),
                accepted, LocalDateTime.now(clock));
        surgery.attachConsent(form);
        return surgeryRepository.save(surgery);
    }

    /**
     * Completes a surgery: needs a surgeon and a valid consent, books the follow-up
     * appointment with the same surgeon (Person 2's AppointmentService, so the usual
     * clash check applies) and audits the cost and status changes.
     */
    public Surgery completeSurgery(Long actorId, Long surgeryId, BigDecimal finalCost,
                                   LocalDateTime followUpStart, int followUpMinutes) {
        Employee actor = findEmployee(actorId);
        Surgery surgery = findSurgery(surgeryId);
        requireStatus(surgery, SurgeryStatus.PLANNED, "complete");
        requireAllowed(actor, surgery);
        if (!surgery.hasValidConsent()) {
            throw new ConsentRequiredException(surgeryId);
        }

        MedicalRecord record = surgery.getMedicalRecord();
        Appointment followUp = appointmentService.book(record.getAnimal().getId(),
                surgery.getPerformedBy().getId(), locationIdOf(actor, record),
                followUpStart, followUpMinutes, AppointmentType.FOLLOW_UP);

        auditService.record(actor, "Surgery", surgery.getId(), "cost", surgery.getCost(), finalCost);
        auditService.record(actor, "Surgery", surgery.getId(), "status",
                surgery.getStatus(), SurgeryStatus.COMPLETED);
        surgery.complete(finalCost, followUp);
        return surgeryRepository.save(surgery);
    }

    public Surgery cancelSurgery(Long actorId, Long surgeryId) {
        Employee actor = findEmployee(actorId);
        Surgery surgery = findSurgery(surgeryId);
        requireStatus(surgery, SurgeryStatus.PLANNED, "cancel");
        requireAllowed(actor, surgery);

        auditService.record(actor, "Surgery", surgery.getId(), "status",
                surgery.getStatus(), SurgeryStatus.CANCELLED);
        surgery.cancel();
        return surgeryRepository.save(surgery);
    }

    // ------------------------------------------------------------------ costs

    /** Corrects the cost of any treatment. Receptionists cannot; every change is audited. */
    public Treatment changeCost(Long actorId, Long treatmentId, BigDecimal newCost) {
        Employee actor = findEmployee(actorId);
        if (actor.getRole() == StaffRole.RECEPTIONIST) {
            throw new UnauthorizedActionException("Receptionists cannot change treatment costs");
        }
        Treatment treatment = treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new TreatmentNotFoundException(treatmentId));

        auditService.record(actor, treatment.getClass().getSimpleName(), treatment.getId(),
                "cost", treatment.getCost(), newCost);
        treatment.changeCost(newCost);
        return treatmentRepository.save(treatment);
    }

    // ---------------------------------------------------------------- helpers

    private void requireAllowed(Employee actor, Treatment treatment) {
        if (!treatment.canBePerformedBy(actor)) {
            throw new UnauthorizedActionException(actor.getName() + " (" + actor.getRole()
                    + ") is not allowed to perform this " + treatment.getClass().getSimpleName().toLowerCase());
        }
    }

    private void requireStatus(Surgery surgery, SurgeryStatus expected, String action) {
        if (surgery.getStatus() != expected) {
            throw new InvalidSurgeryStateException("Cannot " + action + " surgery " + surgery.getId()
                    + " because it is " + surgery.getStatus());
        }
    }

    /** The visit's location if it came from an appointment, otherwise where the employee works. */
    private Long locationIdOf(Employee actor, MedicalRecord record) {
        Location location = record.getAppointment() != null
                ? record.getAppointment().getLocation()
                : actor.getLocation();
        if (location == null) {
            throw new BusinessRuleException(
                    "Cannot tell which location this treatment happened at: link the record to an "
                            + "appointment or assign " + actor.getName() + " to a location");
        }
        return location.getId();
    }

    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + employeeId));
    }

    private Surgery findSurgery(Long surgeryId) {
        return surgeryRepository.findById(surgeryId)
                .orElseThrow(() -> new TreatmentNotFoundException(surgeryId));
    }
}