package com.example.VetClinic.vaccination.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateScheduleRequest(
        @NotBlank String animalId,
        @NotNull Long protocolId,
        @NotNull LocalDate firstDueDate) {
}
