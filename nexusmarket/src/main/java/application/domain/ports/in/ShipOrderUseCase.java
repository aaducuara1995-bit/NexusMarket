package application.domain.ports.in;

import application.domain.entities.InventoryMovement;
import application.domain.entities.InventoryReservation;
import application.domain.entities.Order;
import application.domain.entities.Shipment;
import application.domain.entities.User;

import java.util.List;

/** Input port: the logistic operator ships an order and the reserved stock is deducted. */
public interface ShipOrderUseCase {

    List<InventoryMovement> ship(User actor, Order order, Shipment shipment,
                                         List<InventoryReservation> reservations);
}