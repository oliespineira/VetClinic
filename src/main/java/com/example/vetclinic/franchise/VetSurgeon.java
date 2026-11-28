package com.example.vetclinic.franchise;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** A vet who can do everything a general vet does, and also perform surgery. */
@Entity
@DiscriminatorValue("VET_SURGEON")
public class VetSurgeon extends Employee {

    protected VetSurgeon() {
    }

    public VetSurgeon(Long id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public StaffRole getRole() {
        return StaffRole.VET_SURGEON;
    }

    @Override
    public boolean canTreatAnimals() {
        return true;
    }

    @Override
    public boolean canDispensePrescriptionProducts() {
        return true;
    }

    @Override
    public boolean canPerformSurgery() {
        return true;
    }
}
