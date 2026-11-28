package com.example.vetclinic.medical.appointment;

import com.example.vetclinic.clientsandpatients.Animal;
import com.example.vetclinic.franchise.Employee;
import com.example.vetclinic.franchise.Location;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An appointment for an animal with a vet at a clinic location.
 *
 * The database generates the numeric id, so two bookings at the same time can never
 * get the same id. Users see it as a reference like APT-0001 (see {@link #getReference()}).
 */
@Entity
public class Appointment {

    private static final String REFERENCE_PREFIX = "APT-";
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("APT-(\\d+)");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime start;

    @Column(nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id")
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vet_id")
    private Employee vet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id")
    private Location location;

    protected Appointment() {
    }

    public Appointment(Animal animal, Employee vet, Location location,
                       LocalDateTime start, int durationMinutes, AppointmentType type) {
        this.animal = animal;
        this.vet = vet;
        this.location = location;
        this.start = start;
        this.durationMinutes = durationMinutes;
        this.type = type;
    }

    /** Turns a database id into the reference users see, e.g. 7 into APT-0007. */
    public static String toReference(Long id) {
        return id == null ? null : String.format(REFERENCE_PREFIX + "%04d", id);
    }

    /** Turns a reference like APT-0007 back into the database id, or returns null if it is not valid. */
    public static Long parseReference(String reference) {
        if (reference == null) {
            return null;
        }
        Matcher matcher = REFERENCE_PATTERN.matcher(reference.trim().toUpperCase());
        if (!matcher.matches()) {
            return null;
        }
        try {
            return Long.valueOf(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getReference() {
        return toReference(id);
    }

    public LocalDateTime getEnd() {
        return start.plusMinutes(durationMinutes);
    }

    /**
     * Half-open interval check [start, end): an appointment ending at 10:00 does not
     * overlap one starting at 10:00, so back-to-back slots are allowed.
     */
    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return start.isBefore(otherEnd) && otherStart.isBefore(getEnd());
    }

    void reschedule(LocalDateTime newStart, int newDurationMinutes) {
        this.start = newStart;
        this.durationMinutes = newDurationMinutes;
    }

    void changeStatus(AppointmentStatus newStatus) {
        this.status = newStatus;
    }

    public Long getId() { return id; }
    public LocalDateTime getStart() { return start; }
    public int getDurationMinutes() { return durationMinutes; }
    public AppointmentType getType() { return type; }
    public AppointmentStatus getStatus() { return status; }
    public Animal getAnimal() { return animal; }
    public Employee getVet() { return vet; }
    public Location getLocation() { return location; }
}
