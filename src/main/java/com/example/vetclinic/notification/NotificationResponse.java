package com.example.vetclinic.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String recipient,
        String message,
        LocalDateTime sentAt,
        Long vaccinationScheduleId) {

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getType(), n.getRecipient(), n.getMessage(), n.getSentAt(),
                n.getVaccinationSchedule() != null ? n.getVaccinationSchedule().getId() : null);
    }
}
