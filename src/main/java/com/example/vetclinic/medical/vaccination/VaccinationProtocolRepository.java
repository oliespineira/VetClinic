package com.example.vetclinic.medical.vaccination;

import com.example.vetclinic.clientsandpatients.Species;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaccinationProtocolRepository extends JpaRepository<VaccinationProtocol, Long> {

    List<VaccinationProtocol> findBySpecies(Species species);
}
