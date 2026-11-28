package com.example.vetclinic.medical.treatment.dto;

import com.example.vetclinic.medical.treatment.Surgery;
import com.example.vetclinic.medical.treatment.Treatment;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One treatment as the API returns it. The surgery fields are null for vaccines
 * and medication. Returning DTOs instead of entities keeps lazy JPA relations and
 * internal fields (like password hashes on employees) out of the JSON.
 */
public record TreatmentResponse(
        Long id,
        String type,
        String description,
        LocalDate date,
        BigDecimal cost,
        String performedBy,
        BigDecimal estimatedCost,
        String surgeryStatus,
        Boolean consentSigned,
        String followUpAppointment) {

    public static TreatmentResponse from(Treatment treatment) {
        BigDecimal estimatedCost = null;
        String surgeryStatus = null;
        Boolean consentSigned = null;
        String followUp = null;
        if (treatment instanceof Surgery surgery) {
            estimatedCost = surgery.getEstimatedCost();
            surgeryStatus = surgery.getStatus().name();
            consentSigned = surgery.hasValidConsent();
            followUp = surgery.getFollowUp() == null ? null : surgery.getFollowUp().getReference();
        }
        return new TreatmentResponse(
                treatment.getId(),
                treatment.getClass().getSimpleName(),
                treatment.getDescription(),
                treatment.getDate(),
                treatment.getCost(),
                treatment.getPerformedBy().getName(),
                estimatedCost,
                surgeryStatus,
                consentSigned,
                followUp);
    }
}
