package com.example.vetclinic.medical.appointment.exception;

import com.example.vetclinic.common.exception.BusinessRuleException;

/** The appointment request itself is invalid, e.g. a duration of 0 or more than 8 hours. */
public class InvalidAppointmentException extends BusinessRuleException {

    public InvalidAppointmentException(String message) {
        super(message);
    }
}
