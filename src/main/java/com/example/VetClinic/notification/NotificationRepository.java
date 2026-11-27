package com.example.VetClinic.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByVaccinationScheduleIdAndTypeAndSentAtGreaterThanEqual(
            Long scheduleId, NotificationType type, LocalDateTime sentAfter);

    List<Notification> findAllByOrderBySentAtDesc();

    List<Notification> findByTypeOrderBySentAtDesc(NotificationType type);
}
