package application.domain.entities;

import application.domain.enums.OrderStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.valueobjects.Address;

import java.util.UUID;

public class Shipment {

    private final UUID id;
    private final Order order;
    private final Address deliveryAddress;
    private ShipmentStatus status;

    public Shipment(Order order, Address deliveryAddress) {
        if (order == null)
            throw new IllegalArgumentException("The shipment must be linked to an order.");
        if (deliveryAddress == null)
            throw new IllegalArgumentException("Delivery address is required.");
        if (order.getStatus() != OrderStatus.PAID)
            throw new IllegalStateException("A shipment can only be created for a paid order.");

        this.id = UUID.randomUUID();
        this.order = order;
        this.deliveryAddress = deliveryAddress;
        this.status = ShipmentStatus.PENDING;
    }

    public void ship() {
        if (status != ShipmentStatus.PENDING)
            throw new IllegalStateException("Only a pending shipment can be shipped.");
        status = ShipmentStatus.SHIPPED;
    }

    public void deliver() {
        if (status != ShipmentStatus.SHIPPED)
            throw new IllegalStateException("Only a shipped shipment can be delivered.");
        status = ShipmentStatus.DELIVERED;
    }

    public UUID getId() { return id; }
    public Order getOrder() { return order; }
    public Address getDeliveryAddress() { return deliveryAddress; }
    public ShipmentStatus getStatus() { return status; }
}