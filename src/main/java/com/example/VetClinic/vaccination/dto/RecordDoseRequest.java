package com.example.VetClinic.vaccination.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record RecordDoseRequest(@NotNull @PastOrPresent LocalDate doseDate) {
}
