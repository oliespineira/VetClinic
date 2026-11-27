package com.example.vetclinic.common.exception;

import org.springframework.http.HttpStatus;

/** TEMPORARY - see VetClinicException. Keep main's version when rebasing. */
public class NotFoundException extends VetClinicException {

    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
