package com.example.VetClinic.notification;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final ReminderService reminderService;

    public NotificationController(NotificationRepository notificationRepository, ReminderService reminderService) {
        this.notificationRepository = notificationRepository;
        this.reminderService = reminderService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<NotificationResponse> list(@RequestParam(required = false) NotificationType type) {
        List<Notification> result = type == null
                ? notificationRepository.findAllByOrderBySentAtDesc()
                : notificationRepository.findByTypeOrderBySentAtDesc(type);
        return result.stream().map(NotificationResponse::from).toList();
    }

    /** Runs the daily reminder job now instead of waiting for 08:00. */
    @PostMapping("/reminders/run")
    public List<NotificationResponse> runReminders() {
        return reminderService.sendDueVaccinationReminders().stream().map(NotificationResponse::from).toList();
    }
}
