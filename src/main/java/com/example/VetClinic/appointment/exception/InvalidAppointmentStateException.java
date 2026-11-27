package com.example.VetClinic.appointment.exception;

import com.example.VetClinic.common.exception.VetClinicException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** E.g. trying to reschedule or cancel an appointment that is already completed or cancelled. */
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidAppointmentStateException extends VetClinicException {

    public InvalidAppointmentStateException(String message) {
        super(message);
    }
}
