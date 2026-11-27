package com.example.VetClinic.vaccination;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VaccinationScheduleRepository extends JpaRepository<VaccinationSchedule, Long> {

    List<VaccinationSchedule> findByAnimalId(String animalId);

    List<VaccinationSchedule> findByNextDueBetween(LocalDate from, LocalDate to);

    boolean existsByAnimalIdAndProtocolId(String animalId, Long protocolId);
}
