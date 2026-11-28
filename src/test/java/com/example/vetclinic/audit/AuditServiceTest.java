package com.example.vetclinic.audit;

import com.example.vetclinic.common.exception.BusinessRuleException;
import com.example.vetclinic.franchise.VetGeneral;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;

    private AuditService auditService;
    private VetGeneral vet;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-12-01T09:00:00Z"), ZoneOffset.UTC);
        auditService = new AuditService(auditLogRepository, fixedClock);
        vet = new VetGeneral(1L, "Dr Smith", "smith@clinic.com");
    }

    @Test
    void record_savesWhoChangedWhatAndWhen() {
        when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuditLog entry = auditService.record(vet, "Surgery", 20L, "cost",
                new BigDecimal("300.00"), new BigDecimal("340.00"));

        assertThat(entry.getChangedAt()).isEqualTo(LocalDateTime.of(2026, 12, 1, 9, 0));
        assertThat(entry.getActorId()).isEqualTo(1L);
        assertThat(entry.getActorName()).isEqualTo("Dr Smith");
        assertThat(entry.getEntityType()).isEqualTo("Surgery");
        assertThat(entry.getEntityId()).isEqualTo(20L);
        assertThat(entry.getField()).isEqualTo("cost");
        assertThat(entry.getOldValue()).isEqualTo("300");
        assertThat(entry.getNewValue()).isEqualTo("340");
    }

    @Test
    void record_sameAmountWithDifferentScale_writesNothing() {
        AuditLog entry = auditService.record(vet, "Surgery", 20L, "cost",
                new BigDecimal("100"), new BigDecimal("100.00"));

        assertThat(entry).isNull();
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    void record_withoutActor_isRejected() {
        assertThrows(BusinessRuleException.class, () -> auditService.record(null, "Surgery", 20L, "cost",
                new BigDecimal("300.00"), new BigDecimal("340.00")));

        verifyNoInteractions(auditLogRepository);
    }
}
