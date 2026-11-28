package com.example.vetclinic.clientsandpatients;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

/**
 * PLACEHOLDER - to be replaced by the real Animal hierarchy (Dog, Cat, Exotic,
 * FarmAnimal) from the clientsandpatients slice. It only carries what appointments
 * and vaccination schedules use: id, name, species (to pick the right
 * VaccinationProtocols) and the owning Client (who receives reminders).
 */
@Entity
public class Animal {

    @Id
    private String id;

    private String name;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Species species;

    @ManyToOne
    private Client owner;

    protected Animal() {
    }

    public Animal(String id, String name, Species species, Client owner) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.owner = owner;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public Species getSpecies() { return species; }
    public Client getOwner() { return owner; }
}
