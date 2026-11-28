package com.example.vetclinic.franchise;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Books appointments, records payments and sells over-the-counter products. */
@Entity
@DiscriminatorValue("RECEPTIONIST")
public class Receptionist extends Employee {

    protected Receptionist() {
    }

    public Receptionist(Long id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public StaffRole getRole() {
        return StaffRole.RECEPTIONIST;
    }
}
