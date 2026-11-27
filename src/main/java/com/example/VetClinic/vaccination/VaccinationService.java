package com.example.VetClinic.vaccination;

import com.example.VetClinic.clientsandpatients.Animal;
import com.example.VetClinic.clientsandpatients.AnimalRepository;
import com.example.VetClinic.common.exception.ResourceNotFoundException;
import com.example.VetClinic.vaccination.exception.InvalidVaccinationScheduleException;
import com.example.VetClinic.vaccination.exception.VaccinationScheduleNotFoundException;
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
        try {
            return protocolRepository.save(new VaccinationProtocol(
                    vaccineName, species, initialDoses, weeksBetweenDoses, boosterIntervalMonths));
        } catch (IllegalArgumentException e) {
            throw new InvalidVaccinationScheduleException(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<VaccinationProtocol> getProtocols(Species species) {
        return species == null ? protocolRepository.findAll() : protocolRepository.findBySpecies(species);
    }

    /** Puts an animal on one protocol, with the first dose due on {@code firstDueDate}. */
    public VaccinationSchedule createSchedule(String animalId, Long protocolId, LocalDate firstDueDate) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal", animalId));
        VaccinationProtocol protocol = protocolRepository.findById(protocolId)
                .orElseThrow(() -> new ResourceNotFoundException("VaccinationProtocol", protocolId));
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
     * (the UML's Animal.getRequiredVaccines()). Person 1 can call this when a new
     * animal is registered.
     */
    public List<VaccinationSchedule> createRequiredSchedules(String animalId, LocalDate firstDueDate) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal", animalId));
        List<VaccinationSchedule> created = new ArrayList<>();
        for (VaccinationProtocol protocol : protocolRepository.findBySpecies(animal.getSpecies())) {
            if (!scheduleRepository.existsByAnimalIdAndProtocolId(animalId, protocol.getId())) {
                created.add(scheduleRepository.save(new VaccinationSchedule(animal, protocol, firstDueDate)));
            }
        }
        return created;
    }

    /**
     * Records that a dose was given and recalculates the next due date. This is the hook
     * for the Vaccine treatment record (Person 3): when a vaccine is given, call this.
     */
    public VaccinationSchedule recordDose(Long scheduleId, LocalDate doseDate) {
        VaccinationSchedule schedule = getSchedule(scheduleId);
        try {
            schedule.recordDose(doseDate);
        } catch (IllegalArgumentException e) {
            throw new InvalidVaccinationScheduleException(e.getMessage());
        }
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
}
