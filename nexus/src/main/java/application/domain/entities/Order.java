package application.domain.entities;

import application.domain.enums.OrderStatus;
import application.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Order {

    private final UUID id;
    private final Buyer buyer;
    private final List<OrderItem> items = new ArrayList<>();
    private final LocalDateTime date;
    private OrderStatus status;

    public Order(Cart cart) {
        if (cart == null)
            throw new IllegalArgumentException("Cart is required.");
        if (cart.getItems().isEmpty())
            throw new IllegalArgumentException("Cannot create an order from an empty cart.");

        this.id = UUID.randomUUID();
        this.buyer = cart.getBuyer();
        cart.getItems().forEach(
                i -> items.add(new OrderItem(i.getProduct(), i.getQuantity())));
        this.date = LocalDateTime.now();
        this.status = OrderStatus.PENDING_PAYMENT;
    }

    public Money calculateTotal() {
        BigDecimal total = items.stream()
                .map(i -> i.subtotal().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Money(total);
    }

    public boolean containsPhysicalProducts() {
        return items.stream().anyMatch(OrderItem::isPhysical);
    }

    public boolean isDigitalOnly() {
        return !containsPhysicalProducts();
    }

    public boolean belongsTo(UUID buyerId) {
        return buyer.getId().equals(buyerId);
    }

    public void pay() {
        requireStatus(OrderStatus.PENDING_PAYMENT, "The order is not pending payment.");
        status = OrderStatus.PAID;
    }

    public void ship() {
        requireStatus(OrderStatus.PAID, "The order must be paid.");
        status = OrderStatus.SHIPPED;
    }

    public void complete() {
        requireStatus(OrderStatus.SHIPPED, "The order must be shipped.");
        status = OrderStatus.DELIVERED;
    }

    private void requireStatus(OrderStatus expected, String message) {
        if (status != expected)
            throw new IllegalStateException(message);
    }

    public UUID getId() { return id; }
    public Buyer getBuyer() { return buyer; }
    public LocalDateTime getDate() { return date; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
}