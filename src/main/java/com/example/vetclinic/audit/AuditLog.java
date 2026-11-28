package com.example.vetclinic.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

/**
 * One line of the audit trail: who changed which field of which record, from what
 * to what, and when. Entries are never edited or deleted, so there are no setters.
 *
 * The employee is stored as id and name (not as a relationship) so the entry
 * still reads correctly even if that employee is later removed.
 */
@Entity
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    private Long actorId;

    @Column(nullable = false)
    private String actorName;

    /** e.g. "Surgery", "Invoice". */
    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    /** e.g. "cost", "status". */
    @Column(name = "field_name", nullable = false)
    private String field;

    private String oldValue;

    private String newValue;

    protected AuditLog() {
    }

    public AuditLog(LocalDateTime changedAt, Long actorId, String actorName, String entityType,
                    Long entityId, String field, String oldValue, String newValue) {
        this.changedAt = changedAt;
        this.actorId = actorId;
        this.actorName = actorName;
        this.entityType = entityType;
        this.entityId = entityId;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Long getId() { return id; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public Long getActorId() { return actorId; }
    public String getActorName() { return actorName; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public String getField() { return field; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
}
