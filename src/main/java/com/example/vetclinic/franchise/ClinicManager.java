package com.example.vetclinic.franchise;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

/** Monitors one clinic: reports, low-stock alerts and approvals. */
@Entity
@DiscriminatorValue("CLINIC_MANAGER")
public class ClinicManager extends Employee {

    @ManyToOne(fetch = FetchType.LAZY)
    private Clinic clinic;

    protected ClinicManager() {
    }

    public ClinicManager(Long id, String name, String email, Clinic clinic) {
        super(id, name, email);
        this.clinic = clinic;
    }

    @Override
    public StaffRole getRole() {
        return StaffRole.CLINIC_MANAGER;
    }

    public Clinic getClinic() { return clinic; }
}
