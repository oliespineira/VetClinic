package com.example.vetclinic.medical.vaccination.dto;

import com.example.vetclinic.clientsandpatients.Species;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProtocolRequest(
        @NotBlank String vaccineName,
        @NotNull Species species,
        @Min(1) int initialDoses,
        @Min(0) int weeksBetweenDoses,
        @Min(1) int boosterIntervalMonths) {
}
