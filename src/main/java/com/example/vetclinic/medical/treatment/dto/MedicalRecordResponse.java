package com.example.vetclinic.medical.treatment.dto;

import com.example.vetclinic.medical.treatment.MedicalRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** One visit with its treatments and total cost, as the API returns it. */
public record MedicalRecordResponse(
        Long id,
        String animalId,
        String animalName,
        LocalDate date,
        BigDecimal weightKg,
        String diagnosis,
        String notes,
        String appointment,
        BigDecimal totalCost,
        List<TreatmentResponse> treatments) {

    public static MedicalRecordResponse from(MedicalRecord record) {
        return new MedicalRecordResponse(
                record.getId(),
                record.getAnimal().getId(),
                record.getAnimal().getName(),
                record.getDate(),
                record.getWeightKg(),
                record.getDiagnosis(),
                record.getNotes(),
                record.getAppointment() == null ? null : record.getAppointment().getReference(),
                record.getTotalCost(),
                record.getTreatments().stream().map(TreatmentResponse::from).toList());
    }
}
