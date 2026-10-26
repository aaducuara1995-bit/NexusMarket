package application.domain.services;

import application.domain.entities.Order;
import application.domain.entities.Shipment;
import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.enums.OrderStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.ports.in.ConfirmDeliveryUseCase;

public class ConfirmDeliveryService implements ConfirmDeliveryUseCase {

    private final AuthorizationService authorization;

    public ConfirmDeliveryService(AuthorizationService authorization) {
        if (authorization == null)
            throw new IllegalArgumentException("Authorization service is required.");
        this.authorization = authorization;
    }

    public void confirm(User actor, Order order, Shipment shipment) {
        authorization.requirePermission(actor, BusinessProcess.SHIPPING_OPERATIONS);
        if (order == null)
            throw new IllegalArgumentException("Order is required.");
        if (shipment == null)
            throw new IllegalArgumentException("Shipment is required.");

        if (!shipment.getOrder().getId().equals(order.getId()))
            throw new IllegalArgumentException("The shipment does not match the order.");
        if (shipment.getStatus() != ShipmentStatus.SHIPPED)
            throw new IllegalStateException("Delivery can only be confirmed for a shipped shipment.");
        if (order.getStatus() != OrderStatus.SHIPPED)
            throw new IllegalStateException("The order must be shipped.");

        shipment.deliver();
        order.complete();
    }
}