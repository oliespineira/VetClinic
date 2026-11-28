package com.example.vetclinic.franchise;

import com.example.vetclinic.common.exception.BusinessRuleException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The franchise. It owns its locations: a location cannot exist without its clinic. */
@Entity
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations = new ArrayList<>();

    protected Clinic() {
    }

    public Clinic(Long id, String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("Clinic name is required");
        }
        this.id = id;
        this.name = name;
    }

    /** Opens a new branch of this clinic and keeps both sides of the relationship in sync. */
    public Location openLocation(String name, String address) {
        Location location = new Location(null, name, address, this);
        locations.add(location);
        return location;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public List<Location> getLocations() { return Collections.unmodifiableList(locations); }
}
