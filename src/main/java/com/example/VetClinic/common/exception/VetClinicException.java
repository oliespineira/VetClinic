package com.example.VetClinic.common.exception;

/**
 * PLACEHOLDER - owned by Person 1 (shared exception setup).
 * Base type for all custom exceptions in the application. Kept minimal so the
 * appointment/vaccination slice compiles; Person 1 may extend it (error codes,
 * @RestControllerAdvice handler, consistent error body).
 */
public class VetClinicException extends RuntimeException {

    public VetClinicException(String message) {
        super(message);
    }
}
