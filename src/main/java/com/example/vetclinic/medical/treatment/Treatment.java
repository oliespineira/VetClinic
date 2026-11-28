package com.example.vetclinic.medical.treatment;

import com.example.vetclinic.common.exception.BusinessRuleException;
import com.example.vetclinic.franchise.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Something done to an animal during a visit, with a cost that ends up on the invoice.
 *
 * Stored with JOINED inheritance: one treatment table for the shared fields and one
 * table per subclass, because Vaccine, Medication and Surgery each add their own columns.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "treatment_date", nullable = false)
    private LocalDate date;

    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Employee performedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private MedicalRecord medicalRecord;

    protected Treatment() {
    }

    protected Treatment(LocalDate date, BigDecimal cost, Employee performedBy) {
        if (date == null) {
            throw new BusinessRuleException("Treatment date is required");
        }
        if (cost == null || cost.signum() < 0) {
            throw new BusinessRuleException("Treatment cost must be zero or positive");
        }
        if (performedBy == null) {
            throw new BusinessRuleException("A treatment must be performed by an employee");
        }
        this.date = date;
        this.cost = cost;
        this.performedBy = performedBy;
    }

    /**
     * Whether this employee is allowed to perform this kind of treatment.
     * By default any vet can; Surgery narrows it to surgeons.
     */
    public boolean canBePerformedBy(Employee employee) {
        return employee.canTreatAnimals();
    }

    /** Short text for the invoice line, e.g. "Rabies vaccine (dose 2)". */
    public abstract String getDescription();

    /** Called by MedicalRecord.addTreatment, so both sides of the relationship stay in sync. */
    void attachTo(MedicalRecord medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    /** Changes the cost. The service writes the audit entry before calling this. */
    public void changeCost(BigDecimal newCost) {
        if (newCost == null || newCost.signum() < 0) {
            throw new BusinessRuleException("Treatment cost must be zero or positive");
        }
        this.cost = newCost;
    }

    public Long getId() { return id; }
    public LocalDate getDate() { return date; }
    public BigDecimal getCost() { return cost; }
    public Employee getPerformedBy() { return performedBy; }
    public MedicalRecord getMedicalRecord() { return medicalRecord; }
}
