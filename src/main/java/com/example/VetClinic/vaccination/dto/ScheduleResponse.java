package com.example.VetClinic.vaccination.dto;

import com.example.VetClinic.vaccination.VaccinationSchedule;

import java.time.LocalDate;

public record ScheduleResponse(
        Long id,
        String animalId,
        String animalName,
        Long protocolId,
        String vaccineName,
        int dosesGiven,
        int initialDoses,
        boolean inPrimarySeries,
        LocalDate lastDoseDate,
        LocalDate nextDue) {

    public static ScheduleResponse from(VaccinationSchedule s) {
        return new ScheduleResponse(
                s.getId(), s.getAnimal().getId(), s.getAnimal().getName(),
                s.getProtocol().getId(), s.getProtocol().getVaccineName(),
                s.getDosesGiven(), s.getProtocol().getInitialDoses(), s.isInPrimarySeries(),
                s.getLastDoseDate(), s.getNextDue());
    }
}
