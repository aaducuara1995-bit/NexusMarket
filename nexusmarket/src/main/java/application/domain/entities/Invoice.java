package application.domain.entities;

import application.domain.enums.OrderStatus;
import application.domain.valueobjects.Money;

import java.time.LocalDateTime;
import java.util.UUID;

public class Invoice {

    private final UUID id;
    private final Order order;
    private final Money total;
    private final LocalDateTime date;

    public Invoice(Order order) {
        if (order == null)
            throw new IllegalArgumentException("The invoice must be linked to an order.");
        if (order.getStatus() == OrderStatus.CART
                || order.getStatus() == OrderStatus.PENDING_PAYMENT)
            throw new IllegalStateException("Only a paid order can be invoiced.");

        this.id = UUID.randomUUID();
        this.order = order;
        this.total = order.calculateTotal();
        this.date = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Order getOrder() { return order; }
    public Money getTotal() { return total; }
    public LocalDateTime getDate() { return date; }
}