package application.domain.ports.in;

import application.domain.entities.Order;
import application.domain.entities.Shipment;
import application.domain.entities.User;

/** Input port: the logistic operator confirms delivery and the order is completed. */
public interface ConfirmDeliveryUseCase {

    void confirm(User actor, Order order, Shipment shipment);
}