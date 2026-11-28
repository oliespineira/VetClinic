package com.example.vetclinic.franchise;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /** Used by the login: employees sign in with their email. */
    Optional<Employee> findByEmail(String email);
}