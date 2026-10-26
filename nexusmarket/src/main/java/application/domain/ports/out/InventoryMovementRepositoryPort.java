package application.domain.ports.out;

import application.domain.entities.InventoryMovement;

import java.util.List;
import java.util.UUID;

public interface InventoryMovementRepositoryPort {

    List<InventoryMovement> findByInventory(UUID inventoryId);

    void saveAll(List<InventoryMovement> movements);
}