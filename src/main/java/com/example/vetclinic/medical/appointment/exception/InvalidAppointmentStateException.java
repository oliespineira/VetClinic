package com.example.vetclinic.medical.appointment.exception;

import com.example.vetclinic.common.exception.ConflictException;

/** E.g. trying to reschedule or cancel an appointment that is already completed or cancelled. */
public class InvalidAppointmentStateException extends ConflictException {

    public InvalidAppointmentStateException(String message) {
        super(message);
    }
}
