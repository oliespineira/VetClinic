package com.example.VetClinic.appointment.dto;

import com.example.VetClinic.appointment.Appointment;
import com.example.VetClinic.appointment.AppointmentStatus;
import com.example.VetClinic.appointment.AppointmentType;

import java.time.LocalDateTime;

public record AppointmentResponse(
        String id,
        LocalDateTime start,
        LocalDateTime end,
        int durationMinutes,
        AppointmentType type,
        AppointmentStatus status,
        String animalId,
        String animalName,
        Long vetId,
        String vetName,
        Long locationId,
        String locationName) {

    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(
                a.getId(), a.getStart(), a.getEnd(), a.getDurationMinutes(), a.getType(), a.getStatus(),
                a.getAnimal().getId(), a.getAnimal().getName(),
                a.getVet().getId(), a.getVet().getName(),
                a.getLocation().getId(), a.getLocation().getName());
    }
}
