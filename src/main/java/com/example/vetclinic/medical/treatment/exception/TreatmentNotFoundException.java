package com.example.vetclinic.medical.treatment.exception;

import com.example.vetclinic.common.exception.NotFoundException;

public class TreatmentNotFoundException extends NotFoundException {

    public TreatmentNotFoundException(Long id) {
        super("Treatment not found: " + id);
    }
}
