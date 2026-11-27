package com.example.VetClinic.appointment;

import com.example.VetClinic.appointment.exception.AppointmentConflictException;
import com.example.VetClinic.appointment.exception.InvalidAppointmentStateException;
import com.example.VetClinic.clientsandpatients.Animal;
import com.example.VetClinic.clientsandpatients.AnimalRepository;
import com.example.VetClinic.clientsandpatients.Client;
import com.example.VetClinic.franchise.Employee;
import com.example.VetClinic.franchise.EmployeeRepository;
import com.example.VetClinic.franchise.Location;
import com.example.VetClinic.franchise.LocationRepository;
import com.example.VetClinic.vaccination.Species;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final LocalDateTime TEN_AM = LocalDateTime.of(2026, 12, 1, 10, 0);

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private AnimalRepository animalRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private LocationRepository locationRepository;

    @InjectMocks private AppointmentService appointmentService;

    private Animal rex;
    private Animal milo;
    private Employee vet;
    private Employee otherVet;
    private Location location;

    @BeforeEach
    void setUp() {
        Client owner = new Client("OWN-0001", "Jane Doe", "jane@example.com", "0123");
        rex = new Animal("PET-0001", "Rex", Species.DOG, owner);
        milo = new Animal("PET-0002", "Milo", Species.CAT, owner);
        vet = new Employee(1L, "Dr Smith", "smith@clinic.com");
        otherVet = new Employee(2L, "Dr Jones", "jones@clinic.com");
        location = new Location(1L, "Main clinic", "1 High St");
    }

    private Appointment existing(String id, Animal animal, Employee staff, LocalDateTime start, int minutes) {
        Appointment a = new Appointment(animal, staff, location, start, minutes, AppointmentType.CHECKUP);
        a.assignId(id);
        return a;
    }

    private void stubLookups(Animal animal, Employee staff) {
        when(animalRepository.findById(animal.getId())).thenReturn(Optional.of(animal));
        when(employeeRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
        when(locationRepository.findById(location.getId())).thenReturn(Optional.of(location));
    }

    @Test
    void overlappingSlotForSameVetIsRejected() {
        stubLookups(milo, vet);
        when(appointmentRepository.findByVetIdAndStatusInAndStartBetween(eq(1L), any(), any(), any()))
                .thenReturn(List.of(existing("APT-0001", rex, vet, TEN_AM, 30)));

        assertThrows(AppointmentConflictException.class, () -> appointmentService.book(
                milo.getId(), vet.getId(), location.getId(), TEN_AM.plusMinutes(15), 30, AppointmentType.CHECKUP));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void backToBackSlotsAreAllowed() {
        stubLookups(milo, vet);
        when(appointmentRepository.findByVetIdAndStatusInAndStartBetween(eq(1L), any(), any(), any()))
                .thenReturn(List.of(existing("APT-0001", rex, vet, TEN_AM, 30)));
        when(appointmentRepository.count()).thenReturn(1L);
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment booked = appointmentService.book(
                milo.getId(), vet.getId(), location.getId(), TEN_AM.plusMinutes(30), 30, AppointmentType.CHECKUP);

        assertThat(booked.getId()).isEqualTo("APT-0002");
        assertThat(booked.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(booked.getEnd()).isEqualTo(TEN_AM.plusMinutes(60));
    }

    @Test
    void sameAnimalCannotBeBookedWithTwoStaffAtOnce() {
        stubLookups(rex, otherVet);
        when(appointmentRepository.findByAnimalIdAndStatusInAndStartBetween(eq("PET-0001"), any(), any(), any()))
                .thenReturn(List.of(existing("APT-0001", rex, vet, TEN_AM, 60)));

        assertThrows(AppointmentConflictException.class, () -> appointmentService.book(
                rex.getId(), otherVet.getId(), location.getId(), TEN_AM.plusMinutes(30), 30, AppointmentType.GROOMING));
    }

    @Test
    void rescheduleIgnoresItselfAndMovesTheSlot() {
        Appointment appointment = existing("APT-0001", rex, vet, TEN_AM, 30);
        when(appointmentRepository.findById("APT-0001")).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findByVetIdAndStatusInAndStartBetween(eq(1L), any(), any(), any()))
                .thenReturn(List.of(appointment));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment moved = appointmentService.reschedule("APT-0001", TEN_AM.plusMinutes(15), 45);

        assertThat(moved.getStart()).isEqualTo(TEN_AM.plusMinutes(15));
        assertThat(moved.getDurationMinutes()).isEqualTo(45);
    }

    @Test
    void rescheduleIntoAnotherAppointmentIsRejected() {
        Appointment appointment = existing("APT-0001", rex, vet, TEN_AM, 30);
        Appointment afternoon = existing("APT-0002", milo, vet, TEN_AM.plusHours(4), 60);
        when(appointmentRepository.findById("APT-0001")).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findByVetIdAndStatusInAndStartBetween(eq(1L), any(), any(), any()))
                .thenReturn(List.of(afternoon));

        assertThrows(AppointmentConflictException.class,
                () -> appointmentService.reschedule("APT-0001", TEN_AM.plusHours(4).plusMinutes(30), 30));
        assertThat(appointment.getStart()).isEqualTo(TEN_AM);
    }

    @Test
    void cancelSetsStatusToCancelled() {
        Appointment appointment = existing("APT-0001", rex, vet, TEN_AM, 30);
        when(appointmentRepository.findById("APT-0001")).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(appointmentService.cancel("APT-0001").getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void completedAppointmentCannotBeCancelledOrRescheduled() {
        Appointment appointment = existing("APT-0001", rex, vet, TEN_AM, 30);
        appointment.changeStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById("APT-0001")).thenReturn(Optional.of(appointment));

        assertThrows(InvalidAppointmentStateException.class, () -> appointmentService.cancel("APT-0001"));
        assertThrows(InvalidAppointmentStateException.class,
                () -> appointmentService.reschedule("APT-0001", TEN_AM.plusDays(1), 30));
    }
}
