package application.adapters.rest.dtos.responses;

import application.domain.enums.AuditOperation;
import application.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponseDTO(UUID id, AuditOperation operation, LocalDateTime date, UUID performedByUserId,
                                  UserRole performedByRole, UUID affectedEntityId, Map<String, String> details) {
}