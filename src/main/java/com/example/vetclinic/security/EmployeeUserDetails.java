package com.example.vetclinic.security;

import com.example.vetclinic.franchise.Employee;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * The logged-in employee as Spring Security sees it. Extends Spring's own User
 * and adds the employee id, so a controller can get it with
 * {@code @AuthenticationPrincipal EmployeeUserDetails me} and pass
 * {@code me.getEmployeeId()} to the services as the actor.
 */
public class EmployeeUserDetails extends User {

    private final Long employeeId;

    public EmployeeUserDetails(Employee employee) {
        super(employee.getEmail(), employee.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name())));
        this.employeeId = employee.getId();
    }

    public Long getEmployeeId() {
        return employeeId;
    }
}
