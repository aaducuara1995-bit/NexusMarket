package application.domain.entities;

import application.domain.enums.MovementType;

import java.time.LocalDateTime;
import java.util.UUID;

public class InventoryMovement {

    private final UUID id;
    private final UUID inventoryId;
    private final UUID productId;
    private final UUID warehouseId;
    private final MovementType type;
    private final int quantity;
    private final String reason;
    private final LocalDateTime date;

    public InventoryMovement(Inventory inventory,
                                MovementType type,
                                int quantity,
                                String reason) {
        if (inventory == null)
            throw new IllegalArgumentException("Inventory is required.");
        if (type == null)
            throw new IllegalArgumentException("Movement type is required.");
        if (quantity == 0)
            throw new IllegalArgumentException("Movement quantity cannot be zero.");
        if (reason == null || reason.isBlank())
            throw new IllegalArgumentException("Movement reason is required.");

        this.id = UUID.randomUUID();
        this.inventoryId = inventory.getId();
        this.productId = inventory.getProduct().getId();
        this.warehouseId = inventory.getWarehouse().getId();
        this.type = type;
        this.quantity = quantity;
        this.reason = reason.trim();
        this.date = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getInventoryId() { return inventoryId; }
    public UUID getProductId() { return productId; }
    public UUID getWarehouseId() { return warehouseId; }
    public MovementType getType() { return type; }
    public int getQuantity() { return quantity; }
    public String getReason() { return reason; }
    public LocalDateTime getDate() { return date; }
}