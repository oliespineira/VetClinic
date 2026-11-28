package com.example.vetclinic.medical.vaccination.exception;

import com.example.vetclinic.common.exception.BusinessRuleException;

/** E.g. a dog put on a cat protocol, the same protocol added twice, or a dose dated in the wrong order. */
public class InvalidVaccinationScheduleException extends BusinessRuleException {

    public InvalidVaccinationScheduleException(String message) {
        super(message);
    }
}
