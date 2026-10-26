package application.adapters.persistence.mongodb;

import application.adapters.persistence.mongodb.mappers.AuditLogMongoMapper;
import application.adapters.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.domain.entities.AuditLog;
import application.domain.ports.out.AuditLogRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Output persistence adapter for the audit trail backed by Spring Data MongoDB
 * (nexusmarket_audit database, audit_logs collection). The domain never depends on
 * MongoDB: AuditLogMongoMapper does the conversion in both directions.
 */
@Repository
public class AuditLogMongoAdapter implements AuditLogRepositoryPort {

    private final AuditLogMongoRepository repository;

    public AuditLogMongoAdapter(AuditLogMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(AuditLog auditLog) {
        repository.save(AuditLogMongoMapper.toDocument(auditLog));
    }

    @Override
    public List<AuditLog> findAll() {
        return repository.findAll().stream().map(AuditLogMongoMapper::toDomain).toList();
    }

    @Override
    public List<AuditLog> findByUser(UUID userId) {
        return repository.findByPerformedByUserId(userId.toString()).stream()
                .map(AuditLogMongoMapper::toDomain).toList();
    }

    @Override
    public List<AuditLog> findByAffectedEntity(UUID entityId) {
        return repository.findByAffectedEntityId(entityId.toString()).stream()
                .map(AuditLogMongoMapper::toDomain).toList();
    }
}