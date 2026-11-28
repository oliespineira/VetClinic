package com.example.vetclinic.medical.treatment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request bodies for the treatment endpoints, grouped in one file because each is a
 * few lines. Bean Validation rejects bad input before it reaches the service, and the
 * global exception handler tells the user which field is wrong.
 * The employee doing the action is never in the body: it comes from the login.
 */
public final class TreatmentRequests {

    private TreatmentRequests() {
    }

    public record OpenRecord(
            @NotBlank String animalId,
            String appointmentReference,
            @NotNull @Positive BigDecimal weightKg,
            String diagnosis,
            String notes) {
    }

    public record GiveVaccine(
            @NotBlank String vaccineName,
            @Min(1) int doseNumber,
            Long vaccinationScheduleId,
            Long productId,
            @NotNull @PositiveOrZero BigDecimal cost) {
    }

    public record GiveMedication(
            @NotBlank String medicineName,
            @NotNull @Positive BigDecimal dosePerKgMg,
            @Min(1) int durationDays,
            Long productId,
            @NotNull @PositiveOrZero BigDecimal cost) {
    }

    public record PlanSurgery(
            @NotBlank String procedure,
            @NotNull @PositiveOrZero BigDecimal estimatedCost) {
    }

    public record SignConsent(boolean accepted) {
    }

    public record CompleteSurgery(
            @NotNull @PositiveOrZero BigDecimal finalCost,
            @NotNull @Future LocalDateTime followUpStart,
            @Min(1) @Max(480) int followUpMinutes) {
    }

    public record ChangeCost(@NotNull @PositiveOrZero BigDecimal newCost) {
    }
}
