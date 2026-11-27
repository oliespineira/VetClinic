package com.example.VetClinic.vaccination.exception;

import com.example.VetClinic.common.exception.VetClinicException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** E.g. a dog put on a cat protocol, or the same protocol added twice for one animal. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidVaccinationScheduleException extends VetClinicException {

    public InvalidVaccinationScheduleException(String message) {
        super(message);
    }
}
