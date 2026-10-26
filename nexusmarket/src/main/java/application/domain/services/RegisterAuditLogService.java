package application.domain.services;

import application.domain.entities.AuditLog;
import application.domain.entities.User;
import application.domain.enums.AuditOperation;
import application.domain.ports.out.AuditLogRepositoryPort;

import java.util.Map;
import java.util.UUID;

/** Leaves the audit record of an operation that has already succeeded. */
public class RegisterAuditLogService {

    private final AuditLogRepositoryPort auditLogs;

    public RegisterAuditLogService(AuditLogRepositoryPort auditLogs) {
        if (auditLogs == null)
            throw new IllegalArgumentException("Audit log repository is required.");
        this.auditLogs = auditLogs;
    }

    public AuditLog register(AuditOperation operation, User performedBy,
                             UUID affectedEntityId, Map<String, String> details) {
        AuditLog auditLog = new AuditLog(operation, performedBy, affectedEntityId, details);
        auditLogs.save(auditLog);
        return auditLog;
    }
}