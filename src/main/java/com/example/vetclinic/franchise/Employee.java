package com.example.vetclinic.franchise;

import com.example.vetclinic.common.exception.BusinessRuleException;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;

/**
 * Anyone who works at a clinic location. Each subclass answers the permission
 * questions for its own role, so the services ask the employee
 * ("can you perform surgery?") instead of checking its type.
 *
 * Stored in one table with an employee_type column (SINGLE_TABLE), because the
 * subclasses add almost no fields of their own.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "employee_type")
public abstract class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    protected Employee() {
    }

    protected Employee(Long id, String name, String email) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("Employee name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new BusinessRuleException("Employee email is not valid: " + email);
        }
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public abstract StaffRole getRole();

    /** Only a VetSurgeon may perform surgery. */
    public boolean canPerformSurgery() {
        return false;
    }

    /** Whether this employee may give out prescription-only products. */
    public boolean canDispensePrescriptionProducts() {
        return false;
    }

    /** Whether this employee may record clinical treatments at all. */
    public boolean canTreatAnimals() {
        return false;
    }

    public void assignTo(Location location) {
        this.location = location;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Location getLocation() { return location; }
}