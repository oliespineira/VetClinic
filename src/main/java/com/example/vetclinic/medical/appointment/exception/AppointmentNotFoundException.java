package com.example.vetclinic.medical.appointment.exception;

import com.example.vetclinic.common.exception.NotFoundException;

public class AppointmentNotFoundException extends NotFoundException {

    public AppointmentNotFoundException(String appointmentReference) {
        super("Appointment not found: " + appointmentReference);
    }
}
