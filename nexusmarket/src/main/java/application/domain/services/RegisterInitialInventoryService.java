package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.InventoryMovement;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.MovementType;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;

public class RegisterInitialInventoryService {

    public record Result(Inventory inventory, List<InventoryMovement> movements) {
    }

    private final InventoryAccessService access;

    public RegisterInitialInventoryService(InventoryAccessService access) {
        if (access == null)
            throw new IllegalArgumentException("Access service is required.");
        this.access = access;
    }

    public Result register(User actor, Seller seller, Product product,
                               Warehouse warehouse, int initialQuantity, List<Inventory> existing) {
        access.requireAccess(actor, seller, product);
        if (warehouse == null)
            throw new IllegalArgumentException("Warehouse is required.");
        if (existing == null)
            throw new IllegalArgumentException("Existing inventory list is required.");

        if (!product.isPhysical())
            throw new IllegalStateException("Only physical products have inventory.");

        if (actor.hasRole(UserRole.SELLER)
                && !(warehouse.isMarketplaceOwned() || warehouse.belongsTo(seller.getId())))
            throw new AccessDeniedException(
                    "A seller can only store stock in their own warehouses or in Marketplace warehouses.");

        boolean alreadyExists = existing.stream()
                .anyMatch(i -> i.isFor(product.getId(), warehouse.getId()));
        if (alreadyExists)
            throw new IllegalStateException("Inventory for this product already exists in that warehouse.");

        Inventory inventory = new Inventory(product, warehouse, initialQuantity);

        List<InventoryMovement> movements = new ArrayList<>();
        if (initialQuantity > 0)
            movements.add(new InventoryMovement(
                    inventory, MovementType.INFLOW, initialQuantity,
                    "Initial stock"));

        return new Result(inventory, movements);
    }
}