package com.example.vetclinic.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sends notifications through the configured {@link NotificationSender} and keeps a
 * record of every one that was sent. Other parts of the system (e.g. low-stock alerts
 * from inventory) can use {@link #send} too.
 */
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationSender notificationSender) {
        this.notificationRepository = notificationRepository;
        this.notificationSender = notificationSender;
    }

    public Notification send(Notification notification) {
        notificationSender.send(notification);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public boolean vaccineReminderSentSince(Long scheduleId, LocalDateTime since) {
        return notificationRepository.existsByVaccinationScheduleIdAndTypeAndSentAtGreaterThanEqual(
                scheduleId, NotificationType.VACCINE_REMINDER, since);
    }

    @Transactional(readOnly = true)
    public List<Notification> findAll(NotificationType type) {
        return type == null
                ? notificationRepository.findAllByOrderBySentAtDesc()
                : notificationRepository.findByTypeOrderBySentAtDesc(type);
    }
}
