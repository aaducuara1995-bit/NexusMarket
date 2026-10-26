package application.adapters.persistence.mongodb.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * MongoDB persistence document (repository DTO) for AuditLog, stored in the
 * audit_logs collection of the nexusmarket_audit database. It is independent from the
 * domain: enums are saved as their names and references as plain identifiers, so the
 * trail stays readable even if the referenced records change.
 */
@Document(collection = "audit_logs")
public class AuditLogDocument {

    @Id
    private String id;

    /** AuditOperation name. */
    private String operation;

    private LocalDateTime date;

    private String performedByUserId;

    /** UserRole name of the acting user at that moment. */
    private String performedByRole;

    private String affectedEntityId;

    private Map<String, String> details = new HashMap<>();

    public AuditLogDocument() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getPerformedByUserId() {
        return performedByUserId;
    }

    public void setPerformedByUserId(String performedByUserId) {
        this.performedByUserId = performedByUserId;
    }

    public String getPerformedByRole() {
        return performedByRole;
    }

    public void setPerformedByRole(String performedByRole) {
        this.performedByRole = performedByRole;
    }

    public String getAffectedEntityId() {
        return affectedEntityId;
    }

    public void setAffectedEntityId(String affectedEntityId) {
        this.affectedEntityId = affectedEntityId;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}