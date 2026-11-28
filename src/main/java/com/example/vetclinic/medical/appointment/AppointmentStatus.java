package com.example.vetclinic.medical.appointment;

/**
 * Lifecycle of an appointment. Only SCHEDULED appointments can be rescheduled,
 * cancelled, completed or marked as no-show.
 */
public enum AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
