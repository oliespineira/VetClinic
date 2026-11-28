package com.example.vetclinic.medical.treatment.exception;

import com.example.vetclinic.common.exception.BusinessRuleException;

/** A surgery cannot be completed without a signed, accepted consent form. */
public class ConsentRequiredException extends BusinessRuleException {

    public ConsentRequiredException(Long surgeryId) {
        super("Surgery " + surgeryId + " has no signed consent form from the owner");
    }
}
