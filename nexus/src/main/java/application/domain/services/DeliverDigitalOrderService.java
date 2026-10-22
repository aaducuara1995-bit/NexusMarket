package application.domain.services;

import application.domain.entities.Order;
import application.domain.enums.OrderStatus;
import application.domain.ports.in.DeliverDigitalOrderUseCase;

public class DeliverDigitalOrderService implements DeliverDigitalOrderUseCase {

    /**
     * Digital products are delivered right after payment, without going through logistics.
     * An order with any physical product follows the regular shipping flow.
     */
    public void deliver(Order order) {
        if (order == null)
            throw new IllegalArgumentException("Order is required.");
        if (!order.isDigitalOnly())
            throw new IllegalStateException(
                    "The order contains physical products and must go through logistics.");
        if (order.getStatus() != OrderStatus.PAID)
            throw new IllegalStateException("Only a paid digital order can be delivered.");

        order.ship();
        order.complete();
    }
}