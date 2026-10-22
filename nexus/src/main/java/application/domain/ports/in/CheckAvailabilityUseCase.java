package application.domain.ports.in;

import application.domain.entities.Inventory;
import application.domain.entities.Product;

import java.util.List;

/** Input port: checks how many units of a product are available across all warehouses. */
public interface CheckAvailabilityUseCase {

    int availableOf(Product product, List<Inventory> inventories);

    boolean hasAvailability(Product product, int quantity, List<Inventory> inventories);
}