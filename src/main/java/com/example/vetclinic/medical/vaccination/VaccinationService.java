package com.example.vetclinic.medical.vaccination;

import com.example.vetclinic.clientsandpatients.Animal;
import com.example.vetclinic.clientsandpatients.AnimalRepository;
import com.example.vetclinic.clientsandpatients.Species;
import com.example.vetclinic.common.exception.NotFoundException;
import com.example.vetclinic.medical.vaccination.exception.InvalidVaccinationScheduleException;
import com.example.vetclinic.medical.vaccination.exception.VaccinationScheduleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class VaccinationService {

    private final VaccinationProtocolRepository protocolRepository;
    private final VaccinationScheduleRepository scheduleRepository;
    private final AnimalRepository animalRepository;

    public VaccinationService(VaccinationProtocolRepository protocolRepository,
                              VaccinationScheduleRepository scheduleRepository,
                              AnimalRepository animalRepository) {
        this.protocolRepository = protocolRepository;
        this.scheduleRepository = scheduleRepository;
        this.animalRepository = animalRepository;
    }

    public VaccinationProtocol createProtocol(String vaccineName, Species species, int initialDoses,
                                              int weeksBetweenDoses, int boosterIntervalMonths) {
        return protocolRepository.save(new VaccinationProtocol(
                vaccineName, species, initialDoses, weeksBetweenDoses, boosterIntervalMonths));
    }

    @Transactional(readOnly = true)
    public List<VaccinationProtocol> getProtocols(Species species) {
        return species == null ? protocolRepository.findAll() : protocolRepository.findBySpecies(species);
    }

    /** Puts an animal on one protocol, with the first dose due on {@code firstDueDate}. */
    public VaccinationSchedule createSchedule(String animalId, Long protocolId, LocalDate firstDueDate) {
        Animal animal = findAnimal(animalId);
        VaccinationProtocol protocol = protocolRepository.findById(protocolId)
                .orElseThrow(() -> new NotFoundException("Vaccination protocol not found: " + protocolId));
        if (protocol.getSpecies() != animal.getSpecies()) {
            throw new InvalidVaccinationScheduleException(
                    protocol.getVaccineName() + " protocol is for " + protocol.getSpecies()
                            + ", but " + animal.getName() + " is a " + animal.getSpecies());
        }
        if (scheduleRepository.existsByAnimalIdAndProtocolId(animalId, protocolId)) {
            throw new InvalidVaccinationScheduleException(
                    animal.getName() + " is already on the " + protocol.getVaccineName() + " protocol");
        }
        return scheduleRepository.save(new VaccinationSchedule(animal, protocol, firstDueDate));
    }

    /**
     * Puts an animal on every protocol for its species that it is not on yet
     * (the UML's Animal.getRequiredVaccines()). Can be called when a new animal
     * is registered.
     */
    public List<VaccinationSchedule> createRequiredSchedules(String animalId, LocalDate firstDueDate) {
        Animal animal = findAnimal(animalId);
        List<VaccinationSchedule> created = new ArrayList<>();
        for (VaccinationProtocol protocol : protocolRepository.findBySpecies(animal.getSpecies())) {
            if (!scheduleRepository.existsByAnimalIdAndProtocolId(animalId, protocol.getId())) {
                created.add(scheduleRepository.save(new VaccinationSchedule(animal, protocol, firstDueDate)));
            }
        }
        return created;
    }

    /**
     * Records that a dose was given and recalculates the next due date. This is the
     * UML's "Vaccine updates dosesGiven and nextDue" link: when a Vaccine treatment
     * is saved, it should call this.
     */
    public VaccinationSchedule recordDose(Long scheduleId, LocalDate doseDate) {
        VaccinationSchedule schedule = getSchedule(scheduleId);
        schedule.recordDose(doseDate);
        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public VaccinationSchedule getSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new VaccinationScheduleNotFoundException(scheduleId));
    }

    @Transactional(readOnly = true)
    public List<VaccinationSchedule> getSchedulesForAnimal(String animalId) {
        return scheduleRepository.findByAnimalId(animalId);
    }

    @Transactional(readOnly = true)
    public List<VaccinationSchedule> getDueBetween(LocalDate from, LocalDate to) {
        return scheduleRepository.findByNextDueBetween(from, to);
    }

    private Animal findAnimal(String animalId) {
        return animalRepository.findById(animalId)
                .orElseThrow(() -> new NotFoundException("Animal not found: " + animalId));
    }
}
