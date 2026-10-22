package application.domain.entities;

import application.domain.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class InventoryReservation {

    private final UUID id;
    private final UUID orderId;
    private final Inventory inventory;
    private final int quantity;
    private final LocalDateTime date;
    private ReservationStatus status;

    private InventoryReservation(UUID orderId, Inventory inventory, int quantity) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.inventory = inventory;
        this.quantity = quantity;
        this.date = LocalDateTime.now();
        this.status = ReservationStatus.ACTIVE;
    }

    public static InventoryReservation create(UUID orderId, Inventory inventory, int quantity) {
        if (orderId == null)
            throw new IllegalArgumentException("The reservation must be linked to an order.");
        if (inventory == null)
            throw new IllegalArgumentException("Inventory is required.");

        inventory.reserve(quantity);
        return new InventoryReservation(orderId, inventory, quantity);
    }

    public void release() {
        requireActive();
        inventory.releaseReservation(quantity);
        status = ReservationStatus.RELEASED;
    }

    public void confirmOutflow() {
        requireActive();
        inventory.confirmOutflow(quantity);
        status = ReservationStatus.CONFIRMED;
    }

    private void requireActive() {
        if (status != ReservationStatus.ACTIVE)
            throw new IllegalStateException("The reservation has already been released or confirmed.");
    }

    public boolean isActive() {
        return status == ReservationStatus.ACTIVE;
    }

    public boolean belongsToOrder(UUID orderIdToCheck) {
        return orderId.equals(orderIdToCheck);
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public Inventory getInventory() { return inventory; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getDate() { return date; }
    public ReservationStatus getStatus() { return status; }
}