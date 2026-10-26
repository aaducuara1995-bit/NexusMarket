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

    private Invoice(UUID id, Order order, Money total, LocalDateTime date) {
        this.id = id;
        this.order = order;
        this.total = total;
        this.date = date;
    }

    /** Rebuilds a invoice saved by a persistence adapter. It is not for creating new ones. */
    public static Invoice restore(UUID id, Order order, Money total, LocalDateTime date) {
        Restore.require(id, "id");
        Restore.require(order, "order");
        Restore.require(total, "total");
        Restore.require(date, "date");
        return new Invoice(id, order, total, date);
    }

    public UUID getId() { return id; }
    public Order getOrder() { return order; }
    public Money getTotal() { return total; }
    public LocalDateTime getDate() { return date; }
}