package com.example.VetClinic.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Writes notifications to the application log instead of sending real emails. */
@Component
public class ConsoleNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationSender.class);

    @Override
    public void send(Notification notification) {
        log.info("[NOTIFICATION] type={} to={} at={} :: {}",
                notification.getType(), notification.getRecipient(),
                notification.getSentAt(), notification.getMessage());
    }
}
