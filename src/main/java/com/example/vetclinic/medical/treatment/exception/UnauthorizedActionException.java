package com.example.vetclinic.medical.treatment.exception;

import com.example.vetclinic.common.exception.VetClinicException;
import org.springframework.http.HttpStatus;

/** An employee tried to do something their role does not allow (403). */
public class UnauthorizedActionException extends VetClinicException {

    public UnauthorizedActionException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
