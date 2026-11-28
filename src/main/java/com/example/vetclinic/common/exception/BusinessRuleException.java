package com.example.vetclinic.common.exception;

import org.springframework.http.HttpStatus;

/** TEMPORARY - see VetClinicException. Keep main's version when rebasing. */
public class BusinessRuleException extends VetClinicException {

    public BusinessRuleException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
