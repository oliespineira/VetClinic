package com.example.vetclinic.notification;

import com.example.vetclinic.clientsandpatients.Animal;
import com.example.vetclinic.clientsandpatients.Client;
import com.example.vetclinic.medical.vaccination.VaccinationSchedule;
import com.example.vetclinic.medical.vaccination.VaccinationScheduleRepository;
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
 * be triggered by hand through NotificationController (handy for the demo).
 *
 * Each schedule gets at most one reminder per due date. Once a dose is recorded,
 * nextDue moves forward (e.g. 12 months for a rabies booster), so a new reminder is
 * sent when the new date gets close. That is what makes reminders recurring.
 *
 * Overdue vaccinations that never got a reminder (for example because the schedule
 * was created with a date already in the past, or the app was not running) get one
 * catch-up reminder saying the dose is overdue.
 */
@Service
public class ReminderService {

    public static final int REMINDER_DAYS_BEFORE_DUE = 7;

    private final VaccinationScheduleRepository scheduleRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    public ReminderService(VaccinationScheduleRepository scheduleRepository,
                           NotificationService notificationService,
                           Clock clock) {
        this.scheduleRepository = scheduleRepository;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @Scheduled(cron = "${vetclinic.reminders.cron:0 0 8 * * *}")
    @Transactional
    public List<Notification> sendDueVaccinationReminders() {
        LocalDate today = LocalDate.now(clock);
        List<Notification> sent = new ArrayList<>();

        for (VaccinationSchedule schedule :
                scheduleRepository.findByNextDueLessThanEqual(today.plusDays(REMINDER_DAYS_BEFORE_DUE))) {
            if (alreadyReminded(schedule)) {
                continue;
            }
            Notification notification = new Notification(
                    NotificationType.VACCINE_REMINDER,
                    recipientFor(schedule.getAnimal()),
                    buildMessage(schedule, today),
                    LocalDateTime.now(clock),
                    schedule);
            sent.add(notificationService.send(notification));
        }
        return sent;
    }

    /** A reminder for this due date was already sent if one exists from one week before it onwards. */
    private boolean alreadyReminded(VaccinationSchedule schedule) {
        LocalDateTime windowStart = schedule.getNextDue()
                .minusDays(REMINDER_DAYS_BEFORE_DUE).atStartOfDay();
        return notificationService.vaccineReminderSentSince(schedule.getId(), windowStart);
    }

    private static String recipientFor(Animal animal) {
        Client owner = animal.getOwner();
        if (owner == null) {
            return "unknown owner";
        }
        return owner.getEmail() != null ? owner.getEmail() : owner.getName();
    }

    private static String buildMessage(VaccinationSchedule schedule, LocalDate today) {
        String dose = schedule.isInPrimarySeries()
                ? "dose " + (schedule.getDosesGiven() + 1) + " of " + schedule.getProtocol().getInitialDoses()
                : "booster";
        String vaccine = schedule.getProtocol().getVaccineName() + " vaccine (" + dose + ")";
        String animalName = schedule.getAnimal().getName();
        if (schedule.getNextDue().isBefore(today)) {
            return "Reminder: " + animalName + "'s " + vaccine + " was due on " + schedule.getNextDue()
                    + " and is overdue. Please book an appointment as soon as possible.";
        }
        return "Reminder: " + animalName + " is due for the " + vaccine + " on "
                + schedule.getNextDue() + ". Please book an appointment.";
    }
}
