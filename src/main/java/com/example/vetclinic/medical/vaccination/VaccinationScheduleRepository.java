package com.example.vetclinic.medical.vaccination;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VaccinationScheduleRepository extends JpaRepository<VaccinationSchedule, Long> {

    List<VaccinationSchedule> findByAnimalId(String animalId);

    List<VaccinationSchedule> findByNextDueBetween(LocalDate from, LocalDate to);

    /** Everything due on or before {@code date}, including overdue schedules. */
    List<VaccinationSchedule> findByNextDueLessThanEqual(LocalDate date);

    boolean existsByAnimalIdAndProtocolId(String animalId, Long protocolId);
}
