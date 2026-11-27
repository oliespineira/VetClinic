package com.example.VetClinic.appointment.exception;

import com.example.VetClinic.common.exception.VetClinicException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AppointmentNotFoundException extends VetClinicException {

    public AppointmentNotFoundException(String appointmentId) {
        super("Appointment not found: " + appointmentId);
    }
}
