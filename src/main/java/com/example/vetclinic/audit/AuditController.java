package com.example.vetclinic.audit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only view of the audit trail. SecurityConfig limits /api/audit/** to clinic managers. */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /** Every change, newest first. */
    @GetMapping
    public List<AuditLogResponse> latest() {
        return auditService.latest().stream().map(AuditLogResponse::from).toList();
    }

    /** The history of one record, e.g. GET /api/audit/Surgery/1 */
    @GetMapping("/{entityType}/{entityId}")
    public List<AuditLogResponse> historyOf(@PathVariable String entityType, @PathVariable Long entityId) {
        return auditService.historyOf(entityType, entityId).stream().map(AuditLogResponse::from).toList();
    }
}
