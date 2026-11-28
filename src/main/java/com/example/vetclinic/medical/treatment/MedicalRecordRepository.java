package com.example.vetclinic.medical.treatment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    /** An animal's medical history: all its records, oldest first. */
    List<MedicalRecord> findByAnimalIdOrderByDateAsc(String animalId);
}

