package com.example.vetclinic.franchise;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * PLACEHOLDER - to be replaced by the real Employee hierarchy (VetGeneral, VetSurgeon,
 * Receptionist, ClinicManager) from the franchise slice. Appointments only need the
 * vet's id and name.
 */
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    protected Employee() {
    }

    public Employee(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
