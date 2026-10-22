package application.domain.entities;

import application.domain.enums.OrderStatus;
import application.domain.enums.RefundStatus;
import application.domain.valueobjects.Money;

import java.util.UUID;

public class Refund {

    private final UUID id;
    private final Order order;
    private final Money amount;
    private RefundStatus status;

    public Refund(Order order, Money amount) {
        if (order == null)
            throw new IllegalArgumentException("The refund must be linked to an order.");
        if (amount == null || amount.amount().signum() <= 0)
            throw new IllegalArgumentException("Refund amount must be greater than zero.");
        if (order.getStatus() == OrderStatus.CART
                || order.getStatus() == OrderStatus.PENDING_PAYMENT)
            throw new IllegalStateException("Cannot refund an order that was not paid.");

        Money orderTotal = order.calculateTotal();
        if (!amount.currency().equals(orderTotal.currency()))
            throw new IllegalArgumentException("The refund must be in the order currency.");
        if (amount.amount().compareTo(orderTotal.amount()) > 0)
            throw new IllegalArgumentException("The refund cannot exceed the order value.");

        this.id = UUID.randomUUID();
        this.order = order;
        this.amount = amount;
        this.status = RefundStatus.PENDING;
    }

    public void process() {
        if (status != RefundStatus.PENDING)
            throw new IllegalStateException("The refund has already been processed.");
        status = RefundStatus.PROCESSED;
    }

    public UUID getId() { return id; }
    public Order getOrder() { return order; }
    public Money getAmount() { return amount; }
    public RefundStatus getStatus() { return status; }
}