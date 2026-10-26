package application.domain.services;

import application.domain.entities.InventoryMovement;
import application.domain.entities.InventoryReservation;
import application.domain.entities.Order;
import application.domain.entities.Shipment;
import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.enums.OrderStatus;
import application.domain.enums.ShipmentStatus;

import java.util.List;

public class ShipOrderService {

    private final AuthorizationService authorization;
    private final InventoryOutflowService inventoryOutflow;

    public ShipOrderService(AuthorizationService authorization,
                           InventoryOutflowService inventoryOutflow) {
        if (authorization == null || inventoryOutflow == null)
            throw new IllegalArgumentException("Supporting services are required.");
        this.authorization = authorization;
        this.inventoryOutflow = inventoryOutflow;
    }

    public List<InventoryMovement> ship(User actor, Order order, Shipment shipment,
                                                List<InventoryReservation> reservations) {
        authorization.requirePermission(actor, BusinessProcess.SHIPPING_OPERATIONS);
        if (order == null)
            throw new IllegalArgumentException("Order is required.");
        if (shipment == null)
            throw new IllegalArgumentException("Shipment is required.");
        if (reservations == null)
            throw new IllegalArgumentException("Reservation list is required.");

        // Everything is validated before any change, so nothing ships halfway.
        if (!shipment.getOrder().getId().equals(order.getId()))
            throw new IllegalArgumentException("The shipment does not match the order.");
        if (order.getStatus() != OrderStatus.PAID)
            throw new IllegalStateException("The order must be paid before shipping.");
        if (shipment.getStatus() != ShipmentStatus.PENDING)
            throw new IllegalStateException("The shipment has already been shipped.");
        if (order.containsPhysicalProducts() && reservations.isEmpty())
            throw new IllegalStateException("The order has no reserved stock to ship.");

        // The inventory outflow validates all reservations before confirming any.
        List<InventoryMovement> movements =
                inventoryOutflow.confirmOutflow(order.getId(), reservations);

        shipment.ship();
        order.ship();
        return movements;
    }
}