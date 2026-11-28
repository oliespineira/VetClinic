package com.example.vetclinic.franchise;

import com.example.vetclinic.common.exception.BusinessRuleException;
import jakarta.persistence.Column;
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

    /** Also the login username, so it must be unique. */
    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    /** BCrypt hash of the login password. Null means this employee cannot log in. */
    @Column(name = "password_hash")
    private String passwordHash;

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

    /** Stores an already-hashed password. Hashing is done by the PasswordEncoder, never here. */
    public void changePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new BusinessRuleException("Password hash cannot be empty");
        }
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Location getLocation() { return location; }
    public String getPasswordHash() { return passwordHash; }
}