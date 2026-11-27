package com.example.VetClinic.appointment.dto;

import com.example.VetClinic.appointment.AppointmentService;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RescheduleAppointmentRequest(
        @NotNull @Future LocalDateTime start,
        @Min(1) @Max(AppointmentService.MAX_DURATION_MINUTES) int durationMinutes) {
}
