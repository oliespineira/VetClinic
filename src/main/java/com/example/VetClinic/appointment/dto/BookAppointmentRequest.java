package com.example.VetClinic.appointment.dto;

import com.example.VetClinic.appointment.AppointmentService;
import com.example.VetClinic.appointment.AppointmentType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookAppointmentRequest(
        @NotBlank String animalId,
        @NotNull Long vetId,
        @NotNull Long locationId,
        @NotNull @Future LocalDateTime start,
        @Min(1) @Max(AppointmentService.MAX_DURATION_MINUTES) int durationMinutes,
        @NotNull AppointmentType type) {
}
