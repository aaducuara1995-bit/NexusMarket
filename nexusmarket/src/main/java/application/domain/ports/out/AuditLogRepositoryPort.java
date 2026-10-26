package application.domain.ports.out;

import application.domain.entities.AuditLog;

import java.util.List;
import java.util.UUID;

/** Append-only audit trail. Implemented with MongoDB in the persistence adapters. */
public interface AuditLogRepositoryPort {
    void save(AuditLog auditLog);
    List<AuditLog> findAll();
    List<AuditLog> findByUser(UUID userId);
    List<AuditLog> findByAffectedEntity(UUID entityId);
}