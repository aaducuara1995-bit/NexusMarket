package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.enums.InventoryStatus;
import application.domain.ports.in.CheckAvailabilityUseCase;

import java.util.List;

public class CheckAvailabilityService implements CheckAvailabilityUseCase {

    public int availableOf(Product product, List<Inventory> inventories) {
        validate(product, inventories);

        return inventories.stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .filter(i -> i.getStatus() == InventoryStatus.AVAILABLE)
                .mapToInt(Inventory::getAvailable)
                .sum();
    }

    public boolean hasAvailability(Product product, int quantity, List<Inventory> inventories) {
        validate(product, inventories);
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero.");

        // Digital products need no inventory (they are delivered after payment).
        if (!product.isPhysical())
            return true;

        return availableOf(product, inventories) >= quantity;
    }

    private static void validate(Product product, List<Inventory> inventories) {
        if (product == null)
            throw new IllegalArgumentException("Product is required.");
        if (inventories == null)
            throw new IllegalArgumentException("Inventory list is required.");
    }
}