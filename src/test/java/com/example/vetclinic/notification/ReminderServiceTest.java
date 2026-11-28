package com.example.vetclinic.notification;

import com.example.vetclinic.clientsandpatients.Animal;
import com.example.vetclinic.clientsandpatients.Client;
import com.example.vetclinic.clientsandpatients.Species;
import com.example.vetclinic.medical.vaccination.VaccinationProtocol;
import com.example.vetclinic.medical.vaccination.VaccinationSchedule;
import com.example.vetclinic.medical.vaccination.VaccinationScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Rome");
    private static final LocalDate TODAY = LocalDate.of(2026, 12, 1);

    @Mock private VaccinationScheduleRepository scheduleRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationSender notificationSender;

    private ReminderService reminderService;
    private Animal rex;
    private VaccinationProtocol rabies;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atTime(8, 0).atZone(ZONE).toInstant(), ZONE);
        NotificationService notificationService = new NotificationService(notificationRepository, notificationSender);
        reminderService = new ReminderService(scheduleRepository, notificationService, clock);

        rex = new Animal("PET-0001", "Rex", Species.DOG,
                new Client("OWN-0001", "Jane Doe", "jane@example.com", "0123"));
        rabies = new VaccinationProtocol("Rabies", Species.DOG, 1, 0, 12);
    }

    private VaccinationSchedule rabiesBoosterDueOn(LocalDate due) {
        VaccinationSchedule schedule = new VaccinationSchedule(rex, rabies, due.minusYears(1));
        schedule.recordDose(due.minusYears(1)); // one dose given, so the 12-month booster is due on `due`
        return schedule;
    }

    @Test
    void reminderIsSentAndLoggedOneWeekBeforeDueDate() {
        VaccinationSchedule schedule = rabiesBoosterDueOn(LocalDate.of(2026, 12, 5));
        when(scheduleRepository.findByNextDueLessThanEqual(TODAY.plusDays(7))).thenReturn(List.of(schedule));
        when(notificationRepository.existsByVaccinationScheduleIdAndTypeAndSentAtGreaterThanEqual(
                any(), eq(NotificationType.VACCINE_REMINDER), eq(LocalDate.of(2026, 11, 28).atStartOfDay())))
                .thenReturn(false);
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Notification> sent = reminderService.sendDueVaccinationReminders();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationSender).send(captor.capture());
        verify(notificationRepository).save(captor.getValue());
        Notification notification = captor.getValue();
        assertThat(sent).containsExactly(notification);
        assertThat(notification.getType()).isEqualTo(NotificationType.VACCINE_REMINDER);
        assertThat(notification.getRecipient()).isEqualTo("jane@example.com");
        assertThat(notification.getMessage()).contains("Rex", "Rabies", "booster", "2026-12-05");
        assertThat(notification.getSentAt()).isEqualTo(LocalDateTime.of(2026, 12, 1, 8, 0));
        assertThat(notification.getVaccinationSchedule()).isSameAs(schedule);
    }

    @Test
    void reminderIsNotSentTwiceForTheSameDueDate() {
        VaccinationSchedule schedule = rabiesBoosterDueOn(LocalDate.of(2026, 12, 5));
        when(scheduleRepository.findByNextDueLessThanEqual(TODAY.plusDays(7))).thenReturn(List.of(schedule));
        when(notificationRepository.existsByVaccinationScheduleIdAndTypeAndSentAtGreaterThanEqual(
                any(), eq(NotificationType.VACCINE_REMINDER), any()))
                .thenReturn(true);

        assertThat(reminderService.sendDueVaccinationReminders()).isEmpty();
        verify(notificationSender, never()).send(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void overdueVaccinationWithoutReminderGetsOneOverdueReminder() {
        VaccinationSchedule overdue = rabiesBoosterDueOn(LocalDate.of(2026, 11, 20));
        when(scheduleRepository.findByNextDueLessThanEqual(TODAY.plusDays(7))).thenReturn(List.of(overdue));
        when(notificationRepository.existsByVaccinationScheduleIdAndTypeAndSentAtGreaterThanEqual(
                any(), eq(NotificationType.VACCINE_REMINDER), eq(LocalDate.of(2026, 11, 13).atStartOfDay())))
                .thenReturn(false);
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Notification> sent = reminderService.sendDueVaccinationReminders();

        assertThat(sent).hasSize(1);
        assertThat(sent.get(0).getMessage()).contains("Rex", "Rabies", "2026-11-20", "overdue");
    }
}
