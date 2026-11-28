package com.example.vetclinic.medical.treatment;

import com.example.vetclinic.common.exception.BusinessRuleException;
import com.example.vetclinic.franchise.Employee;
import com.example.vetclinic.medical.appointment.Appointment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An operation. Only a VetSurgeon can perform it, it needs a signed consent form
 * before it can be completed, and completing it books a follow-up appointment.
 * Keeps the estimated cost apart from the final cost (the inherited cost field).
 */
@Entity
public class Surgery extends Treatment {

    @Column(name = "procedure_name")
    private String procedure;

    private BigDecimal estimatedCost;

    @Enumerated(EnumType.STRING)
    private SurgeryStatus status = SurgeryStatus.PLANNED;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ConsentForm consentForm;

    @OneToOne(fetch = FetchType.LAZY)
    private Appointment followUp;

    protected Surgery() {
    }

    /** A planned surgery. Its final cost starts equal to the estimate until it is completed. */
    public Surgery(LocalDate date, BigDecimal estimatedCost, Employee performedBy, String procedure) {
        super(date, estimatedCost, performedBy);
        if (procedure == null || procedure.isBlank()) {
            throw new BusinessRuleException("Surgery procedure is required");
        }
        this.procedure = procedure;
        this.estimatedCost = estimatedCost;
    }

    /** Narrows the default rule: any vet can treat, but only a surgeon can operate. */
    @Override
    public boolean canBePerformedBy(Employee employee) {
        return employee.canPerformSurgery();
    }

    public boolean hasValidConsent() {
        return consentForm != null && consentForm.isValid();
    }

    public void attachConsent(ConsentForm consentForm) {
        this.consentForm = consentForm;
    }

    /** Marks the surgery done with its final cost and the follow-up appointment the service booked. */
    void complete(BigDecimal finalCost, Appointment followUp) {
        changeCost(finalCost);
        this.followUp = followUp;
        this.status = SurgeryStatus.COMPLETED;
    }

    void cancel() {
        this.status = SurgeryStatus.CANCELLED;
    }

    @Override
    public String getDescription() {
        return "Surgery: " + procedure;
    }

    public String getProcedure() { return procedure; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public SurgeryStatus getStatus() { return status; }
    public ConsentForm getConsentForm() { return consentForm; }
    public Appointment getFollowUp() { return followUp; }
}
