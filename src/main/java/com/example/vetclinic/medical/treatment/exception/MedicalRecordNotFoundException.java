package com.example.vetclinic.medical.treatment.exception;

import com.example.vetclinic.common.exception.NotFoundException;

public class MedicalRecordNotFoundException extends NotFoundException {

    public MedicalRecordNotFoundException(Long id) {
        super("Medical record not found: " + id);
    }
}