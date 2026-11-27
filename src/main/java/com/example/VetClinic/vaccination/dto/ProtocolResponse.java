package com.example.VetClinic.vaccination.dto;

import com.example.VetClinic.vaccination.Species;
import com.example.VetClinic.vaccination.VaccinationProtocol;

public record ProtocolResponse(
        Long id,
        String vaccineName,
        Species species,
        int initialDoses,
        int weeksBetweenDoses,
        int boosterIntervalMonths) {

    public static ProtocolResponse from(VaccinationProtocol p) {
        return new ProtocolResponse(p.getId(), p.getVaccineName(), p.getSpecies(),
                p.getInitialDoses(), p.getWeeksBetweenDoses(), p.getBoosterIntervalMonths());
    }
}
