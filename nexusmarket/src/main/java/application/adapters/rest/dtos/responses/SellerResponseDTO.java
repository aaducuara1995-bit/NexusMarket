package application.adapters.rest.dtos.responses;

import java.util.List;
import java.util.UUID;

public record SellerResponseDTO(UUID id, UUID userId, List<WarehouseResponseDTO> warehouses) {
}