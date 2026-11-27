package com.example.VetClinic.notification;

import com.example.VetClinic.clientsandpatients.Animal;
import com.example.VetClinic.clientsandpatients.Client;
import com.example.VetClinic.vaccination.VaccinationSchedule;
import com.example.VetClinic.vaccination.VaccinationScheduleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends a reminder one week before a vaccination is due. Runs every morning, and can
 * be triggered by hand through ReminderController (handy for the demo).
 *
 * Each schedule gets at most one reminder per due date: once a dose is recorded,
 * nextDue moves forward (e.g. 12 months for a rabies booster), so the next reminder
 * is sent again automatically when the new date gets close. That makes reminders recurring.
 */
@Service
public class ReminderService {

    public static final int REMINDER_DAYS_BEFORE_DUE = 7;

    private final VaccinationScheduleRepository scheduleRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;
    private final Clock clock;

    public ReminderService(VaccinationScheduleRepository scheduleRepository,
                           NotificationRepository notificationRepository,
                           NotificationSender notificationSender,
                           Clock clock) {
        this.scheduleRepository = scheduleRepository;
        this.notificationRepository = notificationRepository;
        this.notificationSender = notificationSender;
        this.clock = clock;
    }

    @Scheduled(cron = "${vetclinic.reminders.cron:0 0 8 * * *}")
    @Transactional
    public List<Notification> sendDueVaccinationReminders() {
        LocalDate today = LocalDate.now(clock);
        List<Notification> sent = new ArrayList<>();

        for (VaccinationSchedule schedule :
                scheduleRepository.findByNextDueBetween(today, today.plusDays(REMINDER_DAYS_BEFORE_DUE))) {
            if (alreadyReminded(schedule)) {
                continue;
            }
            Notification notification = new Notification(
                    NotificationType.VACCINE_REMINDER,
                    recipientFor(schedule.getAnimal()),
                    buildMessage(schedule),
                    LocalDateTime.now(clock),
                    schedule);
            notificationSender.send(notification);
            sent.add(notificationRepository.save(notification));
        }
        return sent;
    }

    /** A reminder for this due date was already sent if one exists inside its reminder window. */
    private boolean alreadyReminded(VaccinationSchedule schedule) {
        LocalDateTime windowStart = schedule.getNextDue()
                .minusDays(REMINDER_DAYS_BEFORE_DUE).atStartOfDay();
        return notificationRepository.existsByVaccinationScheduleIdAndTypeAndSentAtGreaterThanEqual(
                schedule.getId(), NotificationType.VACCINE_REMINDER, windowStart);
    }

    private static String recipientFor(Animal animal) {
        Client owner = animal.getOwner();
        if (owner == null) {
            return "unknown owner";
        }
        return owner.getEmail() != null ? owner.getEmail() : owner.getName();
    }

    private static String buildMessage(VaccinationSchedule schedule) {
        String dose = schedule.isInPrimarySeries()
                ? "dose " + (schedule.getDosesGiven() + 1) + " of " + schedule.getProtocol().getInitialDoses()
                : "booster";
        return "Reminder: " + schedule.getAnimal().getName() + " is due for the "
                + schedule.getProtocol().getVaccineName() + " vaccine (" + dose + ") on "
                + schedule.getNextDue() + ". Please book an appointment.";
    }
}
