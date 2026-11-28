package com.example.vetclinic.medical.treatment;

import com.example.vetclinic.clientsandpatients.Animal;
import com.example.vetclinic.clientsandpatients.AnimalRepository;
import com.example.vetclinic.clientsandpatients.Client;
import com.example.vetclinic.clientsandpatients.Species;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * TEMPORARY - creates one invented owner and dog (OWN-0001 / PET-0001 "Rex") so the
 * treatment endpoints can be demonstrated before Person 1's clients-and-patients
 * sample data exists. Delete this class when that data loader is merged.
 */
@Component
public class TemporaryDemoPatientLoader implements CommandLineRunner {

    private final AnimalRepository animalRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public TemporaryDemoPatientLoader(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (animalRepository.count() > 0) {
            return;
        }
        Client owner = new Client("OWN-0001", "Jane Doe", "jane.doe@example.com", "+34 600 000 000");
        entityManager.persist(owner);
        animalRepository.save(new Animal("PET-0001", "Rex", Species.DOG, owner));
    }
}
