package com.example.vetclinic.medical.treatment;

import com.example.vetclinic.clientsandpatients.Client;
import com.example.vetclinic.common.exception.BusinessRuleException;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

/** The owner's signed agreement to a surgery. A surgery cannot be completed without a valid one. */
@Entity
public class ConsentForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime signedAt;

    private boolean accepted;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Client signedBy;

    protected ConsentForm() {
    }

    public ConsentForm(Client signedBy, boolean accepted, LocalDateTime signedAt) {
        if (signedBy == null) {
            throw new BusinessRuleException("A consent form must be signed by the animal's owner");
        }
        this.signedBy = signedBy;
        this.accepted = accepted;
        this.signedAt = signedAt;
    }

    /** Valid only if the owner accepted and the form has a signature date. */
    public boolean isValid() {
        return accepted && signedAt != null;
    }

    public Long getId() { return id; }
    public LocalDateTime getSignedAt() { return signedAt; }
    public boolean isAccepted() { return accepted; }
    public Client getSignedBy() { return signedBy; }
}
