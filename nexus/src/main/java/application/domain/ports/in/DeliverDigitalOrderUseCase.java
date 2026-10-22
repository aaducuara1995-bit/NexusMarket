package application.domain.ports.in;

import application.domain.entities.Order;

/** Input port: immediately delivers a paid order made only of digital products. */
public interface DeliverDigitalOrderUseCase {

    void deliver(Order order);
}