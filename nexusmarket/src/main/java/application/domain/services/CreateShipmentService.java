package application.domain.services;

import application.domain.entities.Order;
import application.domain.entities.Shipment;
import application.domain.valueobjects.Address;

import java.util.List;

public class CreateShipmentService {

    public Shipment create(Order order, Address deliveryAddress, List<Shipment> existing) {
        if (order == null)
            throw new IllegalArgumentException("Order is required.");
        if (deliveryAddress == null)
            throw new IllegalArgumentException("Delivery address is required.");
        if (existing == null)
            throw new IllegalArgumentException("Existing shipment list is required.");

        // Shipments are logistic processes for physical products only.
        if (order.isDigitalOnly())
            throw new IllegalStateException("Digital product orders have no shipment.");

        if (!order.getBuyer().hasAddress(deliveryAddress))
            throw new IllegalArgumentException("The delivery address does not belong to the buyer.");

        boolean alreadyHasShipment = existing.stream()
                .anyMatch(e -> e.getOrder().getId().equals(order.getId()));
        if (alreadyHasShipment)
            throw new IllegalStateException("The order already has a shipment.");

        // The shipment itself requires the order to be paid.
        return new Shipment(order, deliveryAddress);
    }
}