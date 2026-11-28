package com.example.vetclinic.medical.treatment;

import com.example.vetclinic.common.exception.BusinessRuleException;
import com.example.vetclinic.franchise.Employee;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * A medicine given or prescribed. The dose depends on the animal's weight:
 * dose (mg) = dose per kg (mg/kg) x weight (kg).
 */
@Entity
public class Medication extends Treatment {

    private String medicineName;

    /** mg per kg of body weight, copied from the product when prescribed. */
    private BigDecimal dosePerKgMg;

    /** The calculated dose for this animal, in mg. */
    private BigDecimal doseMg;

    private int durationDays;

    /** The product given out (Person 1's Product). */
    private Long productId;

    protected Medication() {
    }

    public Medication(LocalDate date, BigDecimal cost, Employee performedBy, String medicineName,
                      BigDecimal dosePerKgMg, BigDecimal weightKg, int durationDays, Long productId) {
        super(date, cost, performedBy);
        if (medicineName == null || medicineName.isBlank()) {
            throw new BusinessRuleException("Medicine name is required");
        }
        if (durationDays < 1) {
            throw new BusinessRuleException("Duration must be at least 1 day, but was " + durationDays);
        }
        this.medicineName = medicineName;
        this.dosePerKgMg = dosePerKgMg;
        this.doseMg = calculateDose(dosePerKgMg, weightKg);
        this.durationDays = durationDays;
        this.productId = productId;
    }

    /** dose per kg x weight, rounded to 2 decimals. */
    public static BigDecimal calculateDose(BigDecimal dosePerKgMg, BigDecimal weightKg) {
        if (dosePerKgMg == null || dosePerKgMg.signum() <= 0) {
            throw new BusinessRuleException("Dose per kg must be positive");
        }
        if (weightKg == null || weightKg.signum() <= 0) {
            throw new BusinessRuleException("The animal's weight must be positive to calculate a dose");
        }
        return dosePerKgMg.multiply(weightKg).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getDescription() {
        return medicineName + " " + doseMg + " mg for " + durationDays + " days";
    }

    public String getMedicineName() { return medicineName; }
    public BigDecimal getDosePerKgMg() { return dosePerKgMg; }
    public BigDecimal getDoseMg() { return doseMg; }
    public int getDurationDays() { return durationDays; }
    public Long getProductId() { return productId; }
}
