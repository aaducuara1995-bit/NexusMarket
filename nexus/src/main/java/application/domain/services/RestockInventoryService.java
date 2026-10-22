package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.InventoryMovement;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.MovementType;
import application.domain.enums.ReturnStatus;
import application.domain.ports.in.RestockInventoryUseCase;

import java.util.UUID;

public class RestockInventoryService implements RestockInventoryUseCase {

    private final InventoryAccessService access;

    public RestockInventoryService(InventoryAccessService access) {
        if (access == null)
            throw new IllegalArgumentException("Access service is required.");
        this.access = access;
    }

    public InventoryMovement restock(User actor, Seller seller, Inventory inventory,
                                           int units, ReturnRequest returnRequest) {
        if (inventory == null)
            throw new IllegalArgumentException("Inventory is required.");
        if (returnRequest == null)
            throw new IllegalArgumentException("Return is required.");
        access.requireAccess(actor, seller, inventory.getProduct());

        if (returnRequest.getStatus() != ReturnStatus.APPROVED)
            throw new IllegalStateException("Inventory can only be restocked from an approved return.");
        if (units <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero.");

        UUID productId = inventory.getProduct().getId();
        if (returnRequest.purchasedUnits(productId) == 0)
            throw new IllegalArgumentException("The product does not belong to the returned order.");

        // The return remembers what was already restocked: units are never restocked twice.
        if (units > returnRequest.unitsPendingRestock(productId))
            throw new IllegalArgumentException(
                    "Cannot restock more units than were purchased in the order.");

        inventory.restock(units);
        returnRequest.registerRestock(productId, units);

        return new InventoryMovement(
                inventory, MovementType.RETURN, units,
                "Return of order " + returnRequest.getOrder().getId());
    }
}