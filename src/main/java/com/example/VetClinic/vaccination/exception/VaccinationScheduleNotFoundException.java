package com.example.VetClinic.vaccination.exception;

import com.example.VetClinic.common.exception.VetClinicException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VaccinationScheduleNotFoundException extends VetClinicException {

    public VaccinationScheduleNotFoundException(Long scheduleId) {
        super("Vaccination schedule not found: " + scheduleId);
    }
}
