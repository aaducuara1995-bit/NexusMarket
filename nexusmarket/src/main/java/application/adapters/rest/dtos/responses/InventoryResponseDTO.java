package application.adapters.rest.dtos.responses;

import application.domain.enums.InventoryStatus;

import java.util.UUID;

public record InventoryResponseDTO(UUID id, UUID productId, String productName, UUID warehouseId,
                                   String warehouseName, int quantity, int reserved, int available,
                                   InventoryStatus status) {
}