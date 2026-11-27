package com.example.VetClinic.franchise;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * PLACEHOLDER - staff member (vet or groomer). The UML makes Employee abstract
 * with VetGeneral / VetSurgeon / Receptionist / ClinicManager subclasses; roles are
 * Person 3's slice. Appointment conflict detection works on any Employee, so it
 * already covers vets and groomers.
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
