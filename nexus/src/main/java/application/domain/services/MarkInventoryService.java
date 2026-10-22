package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.ports.in.MarkInventoryUseCase;

public class MarkInventoryService implements MarkInventoryUseCase {

    private final InventoryAccessService access;

    public MarkInventoryService(InventoryAccessService access) {
        if (access == null)
            throw new IllegalArgumentException("Access service is required.");
        this.access = access;
    }

    public void markDamaged(User actor, Seller seller, Inventory inventory) {
        validate(actor, seller, inventory);
        inventory.markDamaged();
    }

    public void block(User actor, Seller seller, Inventory inventory) {
        validate(actor, seller, inventory);
        inventory.block();
    }

    public void enable(User actor, Seller seller, Inventory inventory) {
        validate(actor, seller, inventory);
        inventory.enable();
    }

    private void validate(User actor, Seller seller, Inventory inventory) {
        if (inventory == null)
            throw new IllegalArgumentException("Inventory is required.");
        access.requireAccess(actor, seller, inventory.getProduct());
    }
}