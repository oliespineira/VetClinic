package com.example.vetclinic.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        LocalDateTime changedAt,
        String changedBy,
        String entityType,
        Long entityId,
        String field,
        String oldValue,
        String newValue) {

    public static AuditLogResponse from(AuditLog entry) {
        return new AuditLogResponse(entry.getId(), entry.getChangedAt(), entry.getActorName(),
                entry.getEntityType(), entry.getEntityId(), entry.getField(),
                entry.getOldValue(), entry.getNewValue());
    }
}
