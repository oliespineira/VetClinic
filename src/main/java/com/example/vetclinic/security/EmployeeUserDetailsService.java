package com.example.vetclinic.security;

import com.example.vetclinic.franchise.Employee;
import com.example.vetclinic.franchise.EmployeeRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tells Spring Security how to find a user: employees log in with their email.
 * Because this bean exists, Spring Boot stops creating its default user with a
 * random password.
 */
@Service
public class EmployeeUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public EmployeeUserDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .filter(e -> e.getPasswordHash() != null)
                .orElseThrow(() -> new UsernameNotFoundException("No employee can log in as " + email));
        return new EmployeeUserDetails(employee);
    }
}
