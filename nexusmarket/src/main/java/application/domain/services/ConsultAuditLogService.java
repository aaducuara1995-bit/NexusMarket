package application.domain.services;

import application.domain.entities.AuditLog;
import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.ports.out.AuditLogRepositoryPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Read-only access to the audit trail, reserved for administrative follow-up (OBJ-12). */
public class ConsultAuditLogService {

    private final AuthorizationService authorization;
    private final AuditLogRepositoryPort auditLogs;

    public ConsultAuditLogService(AuthorizationService authorization, AuditLogRepositoryPort auditLogs) {
        if (authorization == null || auditLogs == null)
            throw new IllegalArgumentException("Service dependencies are required.");
        this.authorization = authorization;
        this.auditLogs = auditLogs;
    }

    public List<AuditLog> consultAll(User actor) {
        authorization.requirePermission(actor, BusinessProcess.ADMINISTRATIVE_REPORTS);
        return newestFirst(auditLogs.findAll());
    }

    public List<AuditLog> consultByEntity(User actor, UUID entityId) {
        authorization.requirePermission(actor, BusinessProcess.ADMINISTRATIVE_REPORTS);
        if (entityId == null)
            throw new IllegalArgumentException("The record to consult is required.");
        return newestFirst(auditLogs.findByAffectedEntity(entityId));
    }

    /**
     * Two entries can share the same timestamp when they happen in the same instant, so
     * the list is reversed first: among equal dates, the one saved last comes first.
     */
    private static List<AuditLog> newestFirst(List<AuditLog> entries) {
        List<AuditLog> sorted = new ArrayList<>(entries);
        Collections.reverse(sorted);
        sorted.sort(Comparator.comparing(AuditLog::getDate).reversed());
        return List.copyOf(sorted);
    }
}