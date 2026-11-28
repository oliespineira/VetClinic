package com.example.vetclinic.franchise;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** PLACEHOLDER - to be replaced by the real Location from the franchise slice. */
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
