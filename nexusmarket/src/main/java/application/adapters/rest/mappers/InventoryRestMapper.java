package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.InventoryResponseDTO;
import application.domain.entities.Inventory;

public final class InventoryRestMapper {

    private InventoryRestMapper() {
    }

    public static InventoryResponseDTO toResponseDTO(Inventory inventory) {
        return new InventoryResponseDTO(inventory.getId(), inventory.getProduct().getId(),
                inventory.getProduct().getName(), inventory.getWarehouse().getId(),
                inventory.getWarehouse().getName(), inventory.getQuantity(), inventory.getReserved(),
                inventory.getAvailable(), inventory.getStatus());
    }
}