package com.example.vetclinic.audit;

import com.example.vetclinic.common.exception.BusinessRuleException;
import com.example.vetclinic.franchise.Employee;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Records changes to costs and approvals. Other services call {@link #record}
 * explicitly whenever they change one of those values, so every audited change
 * is visible in the code and easy to unit test.
 */
@Service
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final Clock clock;

    public AuditService(AuditLogRepository auditLogRepository, Clock clock) {
        this.auditLogRepository = auditLogRepository;
        this.clock = clock;
    }

    /**
     * Writes one audit entry. Does nothing if the value did not actually change,
     * so callers can call it unconditionally.
     *
     * @return the saved entry, or null if nothing changed
     */
    public AuditLog record(Employee actor, String entityType, Long entityId,
                           String field, Object oldValue, Object newValue) {
        if (actor == null) {
            throw new BusinessRuleException("An audited change needs the employee who made it");
        }
        if (Objects.equals(asText(oldValue), asText(newValue))) {
            return null;
        }
        AuditLog entry = new AuditLog(LocalDateTime.now(clock), actor.getId(), actor.getName(),
                entityType, entityId, field, asText(oldValue), asText(newValue));
        return auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> historyOf(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByChangedAtAsc(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> latest() {
        return auditLogRepository.findAllByOrderByChangedAtDesc();
    }

    /** BigDecimal.toPlainString avoids "1E+2"; everything else uses toString. */
    private static String asText(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.math.BigDecimal decimal) {
            return decimal.stripTrailingZeros().toPlainString();
        }
        return value.toString();
    }
}