package com.example.vetclinic.medical.appointment;

import com.example.vetclinic.clientsandpatients.Animal;
import com.example.vetclinic.clientsandpatients.AnimalRepository;
import com.example.vetclinic.common.exception.NotFoundException;
import com.example.vetclinic.franchise.Employee;
import com.example.vetclinic.franchise.EmployeeRepository;
import com.example.vetclinic.franchise.Location;
import com.example.vetclinic.franchise.LocationRepository;
import com.example.vetclinic.medical.appointment.exception.AppointmentConflictException;
import com.example.vetclinic.medical.appointment.exception.AppointmentNotFoundException;
import com.example.vetclinic.medical.appointment.exception.InvalidAppointmentException;
import com.example.vetclinic.medical.appointment.exception.InvalidAppointmentStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class AppointmentService {

    /** Longest allowed appointment; also bounds how far back the overlap search looks. */
    public static final int MAX_DURATION_MINUTES = 480;

    /** Statuses that still occupy a time slot. Cancelled and no-show slots are free again. */
    private static final Set<AppointmentStatus> BLOCKING_STATUSES =
            EnumSet.of(AppointmentStatus.SCHEDULED, AppointmentStatus.COMPLETED);

    private final AppointmentRepository appointmentRepository;
    private final AnimalRepository animalRepository;
    private final EmployeeRepository employeeRepository;
    private final LocationRepository locationRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              AnimalRepository animalRepository,
                              EmployeeRepository employeeRepository,
                              LocationRepository locationRepository) {
        this.appointmentRepository = appointmentRepository;
        this.animalRepository = animalRepository;
        this.employeeRepository = employeeRepository;
        this.locationRepository = locationRepository;
    }

    public Appointment book(String animalId, Long vetId, Long locationId,
                            LocalDateTime start, int durationMinutes, AppointmentType type) {
        validateDuration(durationMinutes);
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new NotFoundException("Animal not found: " + animalId));
        Employee vet = employeeRepository.findById(vetId)
                .orElseThrow(() -> new NotFoundException("Vet not found: " + vetId));
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException("Location not found: " + locationId));

        Appointment appointment = new Appointment(animal, vet, location, start, durationMinutes, type);
        checkForConflicts(appointment, start, appointment.getEnd());
        return appointmentRepository.save(appointment);
    }

    public Appointment reschedule(String reference, LocalDateTime newStart, int newDurationMinutes) {
        validateDuration(newDurationMinutes);
        Appointment appointment = getByReference(reference);
        requireScheduled(appointment, "reschedule");
        LocalDateTime newEnd = newStart.plusMinutes(newDurationMinutes);
        checkForConflicts(appointment, newStart, newEnd);
        appointment.reschedule(newStart, newDurationMinutes);
        return appointmentRepository.save(appointment);
    }

    public Appointment cancel(String reference) {
        return transition(reference, AppointmentStatus.CANCELLED, "cancel");
    }

    public Appointment complete(String reference) {
        return transition(reference, AppointmentStatus.COMPLETED, "complete");
    }

    public Appointment markNoShow(String reference) {
        return transition(reference, AppointmentStatus.NO_SHOW, "mark as no-show");
    }

    /** Looks up an appointment by its reference, e.g. APT-0001. */
    @Transactional(readOnly = true)
    public Appointment getByReference(String reference) {
        Long id = Appointment.parseReference(reference);
        if (id == null) {
            throw new AppointmentNotFoundException(reference);
        }
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(reference));
    }

    @Transactional(readOnly = true)
    public List<Appointment> findForVet(Long vetId) {
        return appointmentRepository.findByVetIdOrderByStartAsc(vetId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findForAnimal(String animalId) {
        return appointmentRepository.findByAnimalIdOrderByStartAsc(animalId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findBetween(LocalDateTime from, LocalDateTime to) {
        return appointmentRepository.findByStartBetweenOrderByStartAsc(from, to);
    }

    private Appointment transition(String reference, AppointmentStatus target, String action) {
        Appointment appointment = getByReference(reference);
        requireScheduled(appointment, action);
        appointment.changeStatus(target);
        return appointmentRepository.save(appointment);
    }

    private void requireScheduled(Appointment appointment, String action) {
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new InvalidAppointmentStateException(
                    "Cannot " + action + " appointment " + appointment.getReference()
                            + " because it is " + appointment.getStatus());
        }
    }

    /**
     * Rejects the slot [start, end) if the vet or the animal already has a
     * SCHEDULED/COMPLETED appointment overlapping it. The appointment being
     * rescheduled is ignored so it does not conflict with itself.
     */
    private void checkForConflicts(Appointment candidate, LocalDateTime start, LocalDateTime end) {
        // Anything that overlaps must start after (start - max duration) and before end.
        LocalDateTime searchFrom = start.minusMinutes(MAX_DURATION_MINUTES);

        List<Appointment> vetAppointments = appointmentRepository.findByVetIdAndStatusInAndStartBetween(
                candidate.getVet().getId(), BLOCKING_STATUSES, searchFrom, end);
        findOverlap(vetAppointments, candidate, start, end).ifPresent(existing -> {
            throw new AppointmentConflictException(
                    "Vet " + candidate.getVet().getName() + " already has appointment "
                            + existing.getReference() + " from " + existing.getStart() + " to " + existing.getEnd());
        });

        List<Appointment> animalAppointments = appointmentRepository.findByAnimalIdAndStatusInAndStartBetween(
                candidate.getAnimal().getId(), BLOCKING_STATUSES, searchFrom, end);
        findOverlap(animalAppointments, candidate, start, end).ifPresent(existing -> {
            throw new AppointmentConflictException(
                    "Animal " + candidate.getAnimal().getName() + " already has appointment "
                            + existing.getReference() + " from " + existing.getStart() + " to " + existing.getEnd());
        });
    }

    private static Optional<Appointment> findOverlap(List<Appointment> existing, Appointment candidate,
                                                     LocalDateTime start, LocalDateTime end) {
        return existing.stream()
                .filter(a -> candidate.getId() == null || !Objects.equals(a.getId(), candidate.getId()))
                .filter(a -> a.overlaps(start, end))
                .findFirst();
    }

    private void validateDuration(int durationMinutes) {
        if (durationMinutes <= 0 || durationMinutes > MAX_DURATION_MINUTES) {
            throw new InvalidAppointmentException(
                    "Duration must be between 1 and " + MAX_DURATION_MINUTES + " minutes, but was " + durationMinutes);
        }
    }
}
