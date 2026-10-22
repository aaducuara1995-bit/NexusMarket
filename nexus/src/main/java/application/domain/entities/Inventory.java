package application.domain.entities;

import application.domain.enums.InventoryStatus;

import java.util.UUID;

public class Inventory {

    private final UUID id;
    private final Product product;
    private final Warehouse warehouse;
    private int quantity;
    private int reserved;
    private InventoryStatus status;

    public Inventory(Product product, Warehouse warehouse, int initialQuantity) {
        if (product == null || warehouse == null)
            throw new IllegalArgumentException("Product and warehouse are required.");
        if (initialQuantity < 0)
            throw new IllegalArgumentException("Inventory cannot be negative.");

        this.id = UUID.randomUUID();
        this.product = product;
        this.warehouse = warehouse;
        this.quantity = initialQuantity;
        this.reserved = 0;
        this.status = InventoryStatus.AVAILABLE;
    }

    public int getAvailable() {
        return quantity - reserved;
    }

    public boolean isFor(UUID productId, UUID warehouseId) {
        return product.getId().equals(productId) && warehouse.getId().equals(warehouseId);
    }

    public void receive(int units) {
        validatePositive(units);
        quantity += units;
    }

    public void reserve(int units) {
        validatePositive(units);
        if (status != InventoryStatus.AVAILABLE)
            throw new IllegalStateException("Cannot reserve inventory that is not available.");
        if (getAvailable() < units)
            throw new IllegalStateException("Not enough units to reserve.");

        reserved += units;
    }

    public void releaseReservation(int units) {
        validatePositive(units);
        if (units > reserved)
            throw new IllegalArgumentException("There are not that many reserved units.");

        reserved -= units;
    }

    public void confirmOutflow(int units) {
        validatePositive(units);
        if (units > reserved)
            throw new IllegalArgumentException("The outflow must match an active reservation.");

        reserved -= units;
        quantity -= units;
    }

    public void adjust(int difference) {
        if (difference == 0)
            throw new IllegalArgumentException("The adjustment cannot be zero.");

        int newAddress = quantity + difference;
        if (newAddress < 0)
            throw new IllegalStateException("The adjustment would leave negative stock.");
        if (newAddress < reserved)
            throw new IllegalStateException("The adjustment would leave fewer units than reserved.");

        quantity = newAddress;
    }

    public void restock(int units) {
        validatePositive(units);
        if (status != InventoryStatus.AVAILABLE)
            throw new IllegalStateException("Cannot restock an inventory that is not available.");

        quantity += units;
    }

    public void markDamaged() {
        if (status == InventoryStatus.DAMAGED)
            throw new IllegalStateException("The inventory is already marked as damaged.");
        if (reserved > 0)
            throw new IllegalStateException("Release the reservations before marking the inventory as damaged.");

        status = InventoryStatus.DAMAGED;
    }

    public void block() {
        if (status != InventoryStatus.AVAILABLE)
            throw new IllegalStateException("Only an available inventory can be blocked.");

        status = InventoryStatus.BLOCKED;
    }

    public void enable() {
        if (status != InventoryStatus.BLOCKED)
            throw new IllegalStateException("Only a blocked inventory can be enabled.");

        status = InventoryStatus.AVAILABLE;
    }

    private static void validatePositive(int units) {
        if (units <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero.");
    }

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public Warehouse getWarehouse() { return warehouse; }
    public int getQuantity() { return quantity; }
    public int getReserved() { return reserved; }
    public InventoryStatus getStatus() { return status; }
}