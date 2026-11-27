package com.example.VetClinic.vaccination;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * The rules for one vaccine for one species, e.g. "Rabies for dogs: 1 initial dose,
 * booster every 12 months" or "DHPP for dogs: 3 initial doses 3 weeks apart, then a
 * booster every 12 months".
 */
@Entity
public class VaccinationProtocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Vaccine name, e.g. "Rabies". Not in the UML, but needed for reminder messages. */
    @Column(nullable = false)
    private String vaccineName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Species species;

    /** Number of doses in the primary series before boosters start. */
    private int initialDoses;

    /** Gap between doses of the primary series. */
    private int weeksBetweenDoses;

    /** Gap between boosters once the primary series is complete. */
    private int boosterIntervalMonths;

    protected VaccinationProtocol() {
    }

    public VaccinationProtocol(String vaccineName, Species species, int initialDoses,
                               int weeksBetweenDoses, int boosterIntervalMonths) {
        if (initialDoses < 1) {
            throw new IllegalArgumentException("A protocol needs at least one initial dose");
        }
        if (boosterIntervalMonths < 1) {
            throw new IllegalArgumentException("Booster interval must be at least one month");
        }
        if (initialDoses > 1 && weeksBetweenDoses < 1) {
            throw new IllegalArgumentException("Weeks between doses must be at least one week");
        }
        this.vaccineName = vaccineName;
        this.species = species;
        this.initialDoses = initialDoses;
        this.weeksBetweenDoses = weeksBetweenDoses;
        this.boosterIntervalMonths = boosterIntervalMonths;
    }

    public Long getId() { return id; }
    public String getVaccineName() { return vaccineName; }
    public Species getSpecies() { return species; }
    public int getInitialDoses() { return initialDoses; }
    public int getWeeksBetweenDoses() { return weeksBetweenDoses; }
    public int getBoosterIntervalMonths() { return boosterIntervalMonths; }
}
