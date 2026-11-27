package com.example.VetClinic.franchise;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** PLACEHOLDER - clinic location where an appointment takes place (Franchise section). */
@Entity
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    protected Location() {
    }

    public Location(Long id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
}
