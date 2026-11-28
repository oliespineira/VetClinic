package com.example.vetclinic.franchise;

import com.example.vetclinic.common.exception.BusinessRuleException;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** One branch of the clinic, where employees work and appointments happen. */
@Entity
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    private Clinic clinic;

    protected Location() {
    }

    /** Location not yet attached to a clinic. Kept so existing code and tests still work. */
    public Location(Long id, String name, String address) {
        this(id, name, address, null);
    }

    public Location(Long id, String name, String address, Clinic clinic) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("Location name is required");
        }
        if (address == null || address.isBlank()) {
            throw new BusinessRuleException("Location address is required");
        }
        this.id = id;
        this.name = name;
        this.address = address;
        this.clinic = clinic;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public Clinic getClinic() { return clinic; }
}