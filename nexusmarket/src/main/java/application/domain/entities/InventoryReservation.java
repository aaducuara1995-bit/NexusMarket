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

    private InventoryReservation(UUID id, UUID orderId, Inventory inventory, int quantity,
                                 LocalDateTime date, ReservationStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.inventory = inventory;
        this.quantity = quantity;
        this.date = date;
        this.status = status;
    }

    public static InventoryReservation create(UUID orderId, Inventory inventory, int quantity) {
        if (orderId == null)
            throw new IllegalArgumentException("The reservation must be linked to an order.");
        if (inventory == null)
            throw new IllegalArgumentException("Inventory is required.");

        inventory.reserve(quantity);
        return new InventoryReservation(UUID.randomUUID(), orderId, inventory, quantity,
                LocalDateTime.now(), ReservationStatus.ACTIVE);
    }

    /**
     * Rebuilds a reservation saved by a persistence adapter. Unlike {@link #create}, it does
     * not reserve units again: the stored inventory already counts them.
     */
    public static InventoryReservation restore(UUID id, UUID orderId, Inventory inventory, int quantity,
                                               LocalDateTime date, ReservationStatus status) {
        Restore.require(id, "id");
        Restore.require(orderId, "orderId");
        Restore.require(inventory, "inventory");
        Restore.require(date, "date");
        Restore.require(status, "status");
        if (quantity <= 0)
            throw new IllegalArgumentException("A stored reservation must have units.");
        return new InventoryReservation(id, orderId, inventory, quantity, date, status);
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