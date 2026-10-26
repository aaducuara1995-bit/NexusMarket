package application.domain.entities;

import application.domain.enums.AuditOperation;
import application.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable record of a business operation: who performed it, with which role, when,
 * and on which record. The log only grows; entries are never changed or deleted.
 */
public class AuditLog {

    private final UUID id;
    private final AuditOperation operation;
    private final LocalDateTime date;
    private final UUID performedByUserId;
    private final UserRole performedByRole;
    private final UUID affectedEntityId;
    private final Map<String, String> details;

    public AuditLog(AuditOperation operation, User performedBy,
                    UUID affectedEntityId, Map<String, String> details) {
        if (operation == null)
            throw new IllegalArgumentException("The audited operation is required.");
        if (performedBy == null)
            throw new IllegalArgumentException("The user who performed the operation is required.");
        if (affectedEntityId == null)
            throw new IllegalArgumentException("The affected record is required.");

        this.id = UUID.randomUUID();
        this.operation = operation;
        this.date = LocalDateTime.now();
        this.performedByUserId = performedBy.getId();
        this.performedByRole = performedBy.getRole();
        this.affectedEntityId = affectedEntityId;
        this.details = copy(details);
    }

    private AuditLog(UUID id, AuditOperation operation, LocalDateTime date, UUID performedByUserId,
                     UserRole performedByRole, UUID affectedEntityId, Map<String, String> details) {
        this.id = id;
        this.operation = operation;
        this.date = date;
        this.performedByUserId = performedByUserId;
        this.performedByRole = performedByRole;
        this.affectedEntityId = affectedEntityId;
        this.details = copy(details);
    }

    /** Rebuilds an audit entry saved by a persistence adapter. It is not for creating new ones. */
    public static AuditLog restore(UUID id, AuditOperation operation, LocalDateTime date,
                                   UUID performedByUserId, UserRole performedByRole,
                                   UUID affectedEntityId, Map<String, String> details) {
        Restore.require(id, "id");
        Restore.require(operation, "operation");
        Restore.require(date, "date");
        Restore.require(performedByUserId, "performedByUserId");
        Restore.require(performedByRole, "performedByRole");
        Restore.require(affectedEntityId, "affectedEntityId");
        return new AuditLog(id, operation, date, performedByUserId, performedByRole, affectedEntityId, details);
    }

    private static Map<String, String> copy(Map<String, String> details) {
        return details == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(details));
    }

    public UUID getId() { return id; }
    public AuditOperation getOperation() { return operation; }
    public LocalDateTime getDate() { return date; }
    public UUID getPerformedByUserId() { return performedByUserId; }
    public UserRole getPerformedByRole() { return performedByRole; }
    public UUID getAffectedEntityId() { return affectedEntityId; }
    public Map<String, String> getDetails() { return details; }
}