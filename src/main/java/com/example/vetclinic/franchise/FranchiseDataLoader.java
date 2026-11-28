package com.example.vetclinic.franchise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sample data for the franchise slice, loaded at startup: one clinic with two
 * locations and one employee per role, each with a login. All names and
 * addresses are invented. Does nothing if employees already exist.
 *
 * Logins (also in the running instructions):
 *   reception@pawcare.example / reception123   RECEPTIONIST
 *   vet@pawcare.example       / vet123         VET_GENERAL
 *   surgeon@pawcare.example   / surgeon123     VET_SURGEON
 *   manager@pawcare.example   / manager123     CLINIC_MANAGER
 */
@Component
public class FranchiseDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FranchiseDataLoader.class);

    private final ClinicRepository clinicRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public FranchiseDataLoader(ClinicRepository clinicRepository,
                               EmployeeRepository employeeRepository,
                               PasswordEncoder passwordEncoder) {
        this.clinicRepository = clinicRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            return;
        }

        Clinic clinic = new Clinic(null, "PawCare Veterinary Group");
        Location centro = clinic.openLocation("PawCare Madrid Centro", "Calle de Ejemplo 1, Madrid");
        clinic.openLocation("PawCare Madrid Norte", "Avenida de Prueba 25, Madrid");
        clinicRepository.save(clinic);

        createLogin(new Receptionist(null, "Lucia Moreno", "reception@pawcare.example"), centro, "reception123");
        createLogin(new VetGeneral(null, "Dr. Carlos Ruiz", "vet@pawcare.example"), centro, "vet123");
        createLogin(new VetSurgeon(null, "Dr. Elena Navarro", "surgeon@pawcare.example"), centro, "surgeon123");
        createLogin(new ClinicManager(null, "Marta Gil", "manager@pawcare.example", clinic), centro, "manager123");

        log.info("Franchise sample data loaded: 1 clinic, 2 locations, 4 employees with logins");
    }

    private void createLogin(Employee employee, Location location, String rawPassword) {
        employee.assignTo(location);
        employee.changePasswordHash(passwordEncoder.encode(rawPassword));
        employeeRepository.save(employee);
    }
}
