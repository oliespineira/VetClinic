package com.example.VetClinic.vaccination;

import com.example.VetClinic.clientsandpatients.Animal;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

/**
 * Tracks where one animal is in one VaccinationProtocol: how many doses it has had
 * and when the next one is due. Recording a dose (see {@link #recordDose}) is what
 * the UML calls "updates dosesGiven and nextDue".
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"animal_id", "protocol_id"}))
public class VaccinationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id")
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "protocol_id")
    private VaccinationProtocol protocol;

    private int dosesGiven;

    private LocalDate nextDue;

    /** Date of the most recent dose; null until the first dose. Needed to compute nextDue. */
    private LocalDate lastDoseDate;

    protected VaccinationSchedule() {
    }

    /**
     * @param firstDueDate when the first dose should be given (e.g. at 8 weeks old,
     *                     or the registration date for an adult animal)
     */
    public VaccinationSchedule(Animal animal, VaccinationProtocol protocol, LocalDate firstDueDate) {
        this.animal = animal;
        this.protocol = protocol;
        this.dosesGiven = 0;
        this.nextDue = firstDueDate;
    }

    /**
     * Next due date based on the last dose:
     * <ul>
     *   <li>no dose yet: the first due date stays as it is</li>
     *   <li>primary series not finished: last dose + weeksBetweenDoses</li>
     *   <li>primary series finished: last dose + boosterIntervalMonths (recurring)</li>
     * </ul>
     */
    public LocalDate calculateNextDue() {
        if (lastDoseDate == null) {
            return nextDue;
        }
        if (dosesGiven < protocol.getInitialDoses()) {
            return lastDoseDate.plusWeeks(protocol.getWeeksBetweenDoses());
        }
        return lastDoseDate.plusMonths(protocol.getBoosterIntervalMonths());
    }

    /** Records a dose given on {@code doseDate} and moves nextDue forward. */
    public void recordDose(LocalDate doseDate) {
        if (lastDoseDate != null && doseDate.isBefore(lastDoseDate)) {
            throw new IllegalArgumentException(
                    "Dose date " + doseDate + " is before the previous dose on " + lastDoseDate);
        }
        this.dosesGiven++;
        this.lastDoseDate = doseDate;
        this.nextDue = calculateNextDue();
    }

    /** True while the animal is still in the primary series rather than on boosters. */
    public boolean isInPrimarySeries() {
        return dosesGiven < protocol.getInitialDoses();
    }

    public Long getId() { return id; }
    public Animal getAnimal() { return animal; }
    public VaccinationProtocol getProtocol() { return protocol; }
    public int getDosesGiven() { return dosesGiven; }
    public LocalDate getNextDue() { return nextDue; }
    public LocalDate getLastDoseDate() { return lastDoseDate; }
}
