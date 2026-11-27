package com.example.VetClinic.appointment.exception;

import com.example.VetClinic.common.exception.VetClinicException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** The requested time slot overlaps another appointment of the same staff member or animal. */
@ResponseStatus(HttpStatus.CONFLICT)
public class AppointmentConflictException extends VetClinicException {

    public AppointmentConflictException(String message) {
        super(message);
    }
}
