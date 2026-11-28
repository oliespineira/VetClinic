package com.example.vetclinic.franchise;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** A vet who can treat animals and prescribe, but not operate. */
@Entity
@DiscriminatorValue("VET_GENERAL")
public class VetGeneral extends Employee {

    protected VetGeneral() {
    }

    public VetGeneral(Long id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public StaffRole getRole() {
        return StaffRole.VET_GENERAL;
    }

    @Override
    public boolean canTreatAnimals() {
        return true;
    }

    @Override
    public boolean canDispensePrescriptionProducts() {
        return true;
    }
}
