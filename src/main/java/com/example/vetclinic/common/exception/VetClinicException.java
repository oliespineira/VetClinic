package com.example.vetclinic.common.exception;

import org.springframework.http.HttpStatus;

/**
 * TEMPORARY - stand-in for the shared exception setup that is being added on main
 * (abstract VetClinicException with getStatus(), NotFoundException, ConflictException,
 * BusinessRuleException and a GlobalExceptionHandler). When rebasing onto main, keep
 * main's version of the classes in this package.
 */
public abstract class VetClinicException extends RuntimeException {

    protected VetClinicException(String message) {
        super(message);
    }

    public abstract HttpStatus getStatus();
}
