package com.example.vetclinic.medical.treatment;

import com.example.vetclinic.clientsandpatients.Animal;
import com.example.vetclinic.common.exception.BusinessRuleException;
import com.example.vetclinic.medical.appointment.Appointment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * What happened at one visit: the weight, the diagnosis and the treatments given.
 * An animal's medical history is simply all of its records, ordered by date.
 * The record owns its treatments: removing a record removes them too.
 */
@Entity
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_date", nullable = false)
    private LocalDate date;

    private BigDecimal weightKg;

    private String diagnosis;

    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Animal animal;

    /** The appointment this visit came from, if it was booked. */
    @OneToOne(fetch = FetchType.LAZY)
    private Appointment appointment;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Treatment> treatments = new ArrayList<>();

    protected MedicalRecord() {
    }

    public MedicalRecord(Animal animal, Appointment appointment, LocalDate date,
                         BigDecimal weightKg, String diagnosis, String notes) {
        if (animal == null) {
            throw new BusinessRuleException("A medical record must belong to an animal");
        }
        if (date == null) {
            throw new BusinessRuleException("Medical record date is required");
        }
        if (weightKg == null || weightKg.signum() <= 0) {
            throw new BusinessRuleException("Weight must be positive");
        }
        this.animal = animal;
        this.appointment = appointment;
        this.date = date;
        this.weightKg = weightKg;
        this.diagnosis = diagnosis;
        this.notes = notes;
    }

    public void addTreatment(Treatment treatment) {
        treatment.attachTo(this);
        treatments.add(treatment);
    }

    /** Sum of the costs of every treatment in this visit. */
    public BigDecimal getTotalCost() {
        return treatments.stream()
                .map(Treatment::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public BigDecimal getWeightKg() { return weightKg; }
    public String getDiagnosis() { return diagnosis; }
    public String getNotes() { return notes; }
    public Animal getAnimal() { return animal; }
    public Appointment getAppointment() { return appointment; }
    public List<Treatment> getTreatments() { return Collections.unmodifiableList(treatments); }
}
