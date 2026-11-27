package com.example.VetClinic.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * PLACEHOLDER - owned by Person 1 (shared exception setup).
 * Thrown when a referenced entity (animal, vet, location, ...) does not exist.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends VetClinicException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found: " + id);
    }
}
