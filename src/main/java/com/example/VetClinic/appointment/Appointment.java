package com.example.VetClinic.appointment;

import com.example.VetClinic.clientsandpatients.Animal;
import com.example.VetClinic.franchise.Employee;
import com.example.VetClinic.franchise.Location;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

/**
 * An appointment for an animal with a staff member (vet or groomer) at a clinic location.
 * Ids look like APT-0001.
 */
@Entity
public class Appointment {

    @Id
    private String id;

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

    void assignId(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public LocalDateTime getStart() { return start; }
    public int getDurationMinutes() { return durationMinutes; }
    public AppointmentType getType() { return type; }
    public AppointmentStatus getStatus() { return status; }
    public Animal getAnimal() { return animal; }
    public Employee getVet() { return vet; }
    public Location getLocation() { return location; }
}
