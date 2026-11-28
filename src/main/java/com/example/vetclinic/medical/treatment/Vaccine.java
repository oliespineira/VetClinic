package com.example.vetclinic.medical.treatment;

import com.example.vetclinic.common.exception.BusinessRuleException;
import com.example.vetclinic.franchise.Employee;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One vaccine dose. Saving it moves the animal's VaccinationSchedule forward
 * (Person 2's VaccinationService.recordDose) and takes one unit from stock
 * (Person 1's inventory).
 */
@Entity
public class Vaccine extends Treatment {

    private String vaccineName;

    private int doseNumber;

    /** The VaccinationSchedule this dose belongs to (Person 2's entity). */
    private Long vaccinationScheduleId;

    /** The vaccine product used (Person 1's Product). */
    private Long productId;

    protected Vaccine() {
    }

    public Vaccine(LocalDate date, BigDecimal cost, Employee performedBy, String vaccineName,
                   int doseNumber, Long vaccinationScheduleId, Long productId) {
        super(date, cost, performedBy);
        if (vaccineName == null || vaccineName.isBlank()) {
            throw new BusinessRuleException("Vaccine name is required");
        }
        if (doseNumber < 1) {
            throw new BusinessRuleException("Dose number must be 1 or more, but was " + doseNumber);
        }
        this.vaccineName = vaccineName;
        this.doseNumber = doseNumber;
        this.vaccinationScheduleId = vaccinationScheduleId;
        this.productId = productId;
    }

    @Override
    public String getDescription() {
        return vaccineName + " vaccine (dose " + doseNumber + ")";
    }

    public String getVaccineName() { return vaccineName; }
    public int getDoseNumber() { return doseNumber; }
    public Long getVaccinationScheduleId() { return vaccinationScheduleId; }
    public Long getProductId() { return productId; }
}
