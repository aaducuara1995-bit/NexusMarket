package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.InventoryMovement;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.MovementType;

public class AdjustInventoryService {

    private final InventoryAccessService access;

    public AdjustInventoryService(InventoryAccessService access) {
        if (access == null)
            throw new IllegalArgumentException("Access service is required.");
        this.access = access;
    }

    public InventoryMovement adjust(User actor, Seller seller, Inventory inventory,
                                        int difference, String reason) {
        if (inventory == null)
            throw new IllegalArgumentException("Inventory is required.");
        access.requireAccess(actor, seller, inventory.getProduct());

        // The reason is validated before the inventory is changed.
        if (reason == null || reason.isBlank())
            throw new IllegalArgumentException("Adjustment reason is required.");

        inventory.adjust(difference);

        return new InventoryMovement(
                inventory, MovementType.ADJUSTMENT, difference,
                "Adjustment: " + reason.trim());
    }
}