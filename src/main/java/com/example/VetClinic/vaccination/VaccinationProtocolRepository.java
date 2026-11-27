package com.example.VetClinic.vaccination;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaccinationProtocolRepository extends JpaRepository<VaccinationProtocol, Long> {

    List<VaccinationProtocol> findBySpecies(Species species);
}
