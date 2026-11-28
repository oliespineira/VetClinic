package com.example.vetclinic.medical.vaccination.exception;

import com.example.vetclinic.common.exception.NotFoundException;

public class VaccinationScheduleNotFoundException extends NotFoundException {

    public VaccinationScheduleNotFoundException(Long scheduleId) {
        super("Vaccination schedule not found: " + scheduleId);
    }
}
