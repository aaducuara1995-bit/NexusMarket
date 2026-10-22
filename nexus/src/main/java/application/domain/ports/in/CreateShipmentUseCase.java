package application.domain.ports.in;

import application.domain.entities.Order;
import application.domain.entities.Shipment;
import application.domain.valueobjects.Address;

import java.util.List;

/** Input port: creates the shipment of a paid order with physical products. */
public interface CreateShipmentUseCase {

    Shipment create(Order order, Address deliveryAddress, List<Shipment> existing);
}