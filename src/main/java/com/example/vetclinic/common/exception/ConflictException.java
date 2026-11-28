package com.example.vetclinic.common.exception;

import org.springframework.http.HttpStatus;

/** TEMPORARY - see VetClinicException. Keep main's version when rebasing. */
public class ConflictException extends VetClinicException {

    public ConflictException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
