package com.example.vetclinic.medical.treatment;

import com.example.vetclinic.common.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** The dose formula is a pure static method, so it needs no mocks at all. */
class MedicationTest {

    @Test
    void calculateDose_roundsToTwoDecimals() {
        // 0.333 mg/kg x 10 kg = 3.330 mg, rounded to 3.33
        BigDecimal dose = Medication.calculateDose(new BigDecimal("0.333"), new BigDecimal("10"));

        assertEquals(new BigDecimal("3.33"), dose);
    }

    @Test
    void calculateDose_withZeroWeight_isRejected() {
        assertThrows(BusinessRuleException.class,
                () -> Medication.calculateDose(new BigDecimal("2.5"), BigDecimal.ZERO));
    }
}
