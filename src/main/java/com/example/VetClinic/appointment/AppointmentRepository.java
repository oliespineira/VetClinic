package com.example.VetClinic.appointment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, String> {

    List<Appointment> findByVetIdAndStatusInAndStartBetween(
            Long vetId, Collection<AppointmentStatus> statuses, LocalDateTime from, LocalDateTime to);

    List<Appointment> findByAnimalIdAndStatusInAndStartBetween(
            String animalId, Collection<AppointmentStatus> statuses, LocalDateTime from, LocalDateTime to);

    List<Appointment> findByVetIdOrderByStartAsc(Long vetId);

    List<Appointment> findByAnimalIdOrderByStartAsc(String animalId);

    List<Appointment> findByStartBetweenOrderByStartAsc(LocalDateTime from, LocalDateTime to);
}
