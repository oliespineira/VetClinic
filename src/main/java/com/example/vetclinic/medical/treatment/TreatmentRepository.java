package com.example.vetclinic.medical.treatment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    List<Treatment> findByMedicalRecordId(Long medicalRecordId);
}
