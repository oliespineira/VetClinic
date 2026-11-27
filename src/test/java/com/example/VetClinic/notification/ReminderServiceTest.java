package com.example.VetClinic.notification;

import com.example.VetClinic.clientsandpatients.Animal;
import com.example.VetClinic.clientsandpatients.Client;
import com.example.VetClinic.vaccination.Species;
import com.example.VetClinic.vaccination.VaccinationProtocol;
import com.example.VetClinic.vaccination.VaccinationSchedule;
import com.example.VetClinic.vaccination.VaccinationScheduleRepository;
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
    private VaccinationSchedule rabiesSchedule;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atTime(8, 0).atZone(ZONE).toInstant(), ZONE);
        reminderService = new ReminderService(scheduleRepository, notificationRepository, notificationSender, clock);

        Animal rex = new Animal("PET-0001", "Rex", Species.DOG,
                new Client("OWN-0001", "Jane Doe", "jane@example.com", "0123"));
        VaccinationProtocol rabies = new VaccinationProtocol("Rabies", Species.DOG, 1, 0, 12);
        rabiesSchedule = new VaccinationSchedule(rex, rabies, LocalDate.of(2025, 12, 5));
        rabiesSchedule.recordDose(LocalDate.of(2025, 12, 5)); // next due 2026-12-05, 4 days from TODAY
    }

    @Test
    void reminderIsSentAndLoggedOneWeekBeforeDueDate() {
        when(scheduleRepository.findByNextDueBetween(TODAY, TODAY.plusDays(7))).thenReturn(List.of(rabiesSchedule));
        when(notificationRepository.existsByVaccinationScheduleIdAndTypeAndSentAtGreaterThanEqual(
                any(), eq(NotificationType.VACCINE_REMINDER), eq(LocalDate.of(2026, 11, 28).atStartOfDay())))
                .thenReturn(false);
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Notification> sent = reminderService.sendDueVaccinationReminders();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationSender).send(captor.capture());
        Notification notification = captor.getValue();
        assertThat(sent).containsExactly(notification);
        assertThat(notification.getType()).isEqualTo(NotificationType.VACCINE_REMINDER);
        assertThat(notification.getRecipient()).isEqualTo("jane@example.com");
        assertThat(notification.getMessage()).contains("Rex", "Rabies", "booster", "2026-12-05");
        assertThat(notification.getSentAt()).isEqualTo(LocalDateTime.of(2026, 12, 1, 8, 0));
        assertThat(notification.getVaccinationSchedule()).isSameAs(rabiesSchedule);
    }

    @Test
    void reminderIsNotSentTwiceForTheSameDueDate() {
        when(scheduleRepository.findByNextDueBetween(TODAY, TODAY.plusDays(7))).thenReturn(List.of(rabiesSchedule));
        when(notificationRepository.existsByVaccinationScheduleIdAndTypeAndSentAtGreaterThanEqual(
                any(), eq(NotificationType.VACCINE_REMINDER), any()))
                .thenReturn(true);

        assertThat(reminderService.sendDueVaccinationReminders()).isEmpty();
        verify(notificationSender, never()).send(any());
        verify(notificationRepository, never()).save(any());
    }
}
