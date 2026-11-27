package com.example.vetclinic.notification;

/**
 * Delivers a notification to its recipient. The project ships a console/log
 * implementation; an email or SMS sender could be swapped in without touching
 * the reminder logic.
 */
public interface NotificationSender {

    void send(Notification notification);
}
