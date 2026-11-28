package com.example.vetclinic.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Login with HTTP Basic (email + password on every request), no sessions.
 *
 * Two layers of access control:
 *  1. Here, coarse rules per URL: you must be logged in, and only managers see the audit log.
 *  2. In the services, the fine rules ("only a surgeon operates", "receptionists cannot
 *     give out prescription products"). Those live in the services so they are unit tested
 *     and cannot be skipped by calling the service from somewhere else.
 *
 * CSRF protection is off because this is a stateless REST API authenticated with a
 * header on every request, not a browser session with cookies, which is what CSRF attacks.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Spring Boot forwards errors to /error. Without this line, a 404 or 500
                        // would turn into a confusing 401, because there is no session to reuse.
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/audit/**").hasRole("CLINIC_MANAGER")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
