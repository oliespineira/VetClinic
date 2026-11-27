package com.example.VetClinic.notification;

import com.example.VetClinic.vaccination.VaccinationSchedule;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

/**
 * A message that was sent (and logged) by the system. Vaccine reminders link to the
 * schedule that triggered them; other types (e.g. low-stock alerts) leave it null.
 */
@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    /** Who it was sent to, e.g. the owner's email address. */
    private String recipient;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccination_schedule_id")
    private VaccinationSchedule vaccinationSchedule;

    protected Notification() {
    }

    public Notification(NotificationType type, String recipient, String message,
                        LocalDateTime sentAt, VaccinationSchedule vaccinationSchedule) {
        this.type = type;
        this.recipient = recipient;
        this.message = message;
        this.sentAt = sentAt;
        this.vaccinationSchedule = vaccinationSchedule;
    }

    public Long getId() { return id; }
    public NotificationType getType() { return type; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public LocalDateTime getSentAt() { return sentAt; }
    public VaccinationSchedule getVaccinationSchedule() { return vaccinationSchedule; }
}
