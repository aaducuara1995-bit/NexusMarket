package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.AuditLogResponseDTO;
import application.domain.entities.AuditLog;

public final class AuditLogRestMapper {

    private AuditLogRestMapper() {
    }

    public static AuditLogResponseDTO toResponseDTO(AuditLog entry) {
        return new AuditLogResponseDTO(entry.getId(), entry.getOperation(), entry.getDate(),
                entry.getPerformedByUserId(), entry.getPerformedByRole(), entry.getAffectedEntityId(),
                entry.getDetails());
    }
}