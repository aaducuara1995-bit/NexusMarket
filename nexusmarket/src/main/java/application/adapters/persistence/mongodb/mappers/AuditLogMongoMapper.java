package application.adapters.persistence.mongodb.mappers;

import application.adapters.persistence.mongodb.documents.AuditLogDocument;
import application.domain.entities.AuditLog;
import application.domain.enums.AuditOperation;
import application.domain.enums.UserRole;

import java.util.HashMap;
import java.util.UUID;

/** Bidirectional mapper between the AuditLog domain entity and its MongoDB document. */
public final class AuditLogMongoMapper {

    private AuditLogMongoMapper() {
    }

    public static AuditLogDocument toDocument(AuditLog domain) {
        AuditLogDocument document = new AuditLogDocument();
        document.setId(domain.getId().toString());
        document.setOperation(domain.getOperation().name());
        document.setDate(domain.getDate());
        document.setPerformedByUserId(domain.getPerformedByUserId().toString());
        document.setPerformedByRole(domain.getPerformedByRole().name());
        document.setAffectedEntityId(domain.getAffectedEntityId().toString());
        document.setDetails(new HashMap<>(domain.getDetails()));
        return document;
    }

    public static AuditLog toDomain(AuditLogDocument document) {
        return AuditLog.restore(UUID.fromString(document.getId()),
                AuditOperation.valueOf(document.getOperation()),
                document.getDate(),
                UUID.fromString(document.getPerformedByUserId()),
                UserRole.valueOf(document.getPerformedByRole()),
                UUID.fromString(document.getAffectedEntityId()),
                document.getDetails());
    }
}