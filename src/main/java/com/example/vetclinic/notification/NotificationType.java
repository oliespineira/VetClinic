package com.example.vetclinic.notification;

public enum NotificationType {
    /** Sent one week before a VaccinationSchedule's nextDue date. */
    VACCINE_REMINDER,
    /** For the Inventory slice: raised when stock falls below the minimum. */
    LOW_STOCK_ALERT
}
