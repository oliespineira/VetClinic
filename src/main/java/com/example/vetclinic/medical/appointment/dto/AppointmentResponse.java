package com.example.vetclinic.medical.appointment.dto;

import com.example.vetclinic.medical.appointment.Appointment;
import com.example.vetclinic.medical.appointment.AppointmentStatus;
import com.example.vetclinic.medical.appointment.AppointmentType;

import java.time.LocalDateTime;

public record AppointmentResponse(
        String id, // reference such as APT-0001
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
                a.getReference(), a.getStart(), a.getEnd(), a.getDurationMinutes(), a.getType(), a.getStatus(),
                a.getAnimal().getId(), a.getAnimal().getName(),
                a.getVet().getId(), a.getVet().getName(),
                a.getLocation().getId(), a.getLocation().getName());
    }
}
