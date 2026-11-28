package com.example.vetclinic.medical.appointment.exception;

import com.example.vetclinic.common.exception.ConflictException;

/** The requested time slot overlaps another appointment of the same vet or animal. */
public class AppointmentConflictException extends ConflictException {

    public AppointmentConflictException(String message) {
        super(message);
    }
}
