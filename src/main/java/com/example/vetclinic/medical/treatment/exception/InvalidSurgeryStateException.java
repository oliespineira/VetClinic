package com.example.vetclinic.medical.treatment.exception;

import com.example.vetclinic.common.exception.ConflictException;

/** The surgery is not in a state that allows this action, e.g. completing a cancelled one. */
public class InvalidSurgeryStateException extends ConflictException {

    public InvalidSurgeryStateException(String message) {
        super(message);
    }
}
