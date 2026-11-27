package com.example.vetclinic.notification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final ReminderService reminderService;

    public NotificationController(NotificationService notificationService, ReminderService reminderService) {
        this.notificationService = notificationService;
        this.reminderService = reminderService;
    }

    @GetMapping
    public List<NotificationResponse> list(@RequestParam(required = false) NotificationType type) {
        return notificationService.findAll(type).stream().map(NotificationResponse::from).toList();
    }

    /** Runs the daily reminder job now instead of waiting for 08:00. */
    @PostMapping("/reminders/run")
    public List<NotificationResponse> runReminders() {
        return reminderService.sendDueVaccinationReminders().stream().map(NotificationResponse::from).toList();
    }
}
