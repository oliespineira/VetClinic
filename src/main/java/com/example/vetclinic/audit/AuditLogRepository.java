package com.example.vetclinic.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** The full history of one record, oldest change first. */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByChangedAtAsc(String entityType, Long entityId);

    List<AuditLog> findAllByOrderByChangedAtDesc();
}
