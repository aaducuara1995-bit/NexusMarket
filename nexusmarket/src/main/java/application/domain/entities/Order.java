package application.domain.entities;

import application.domain.enums.OrderStatus;
import application.domain.valueobjects.Address;
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
    private final Address deliveryAddress;
    private OrderStatus status;

    public Order(Cart cart) {
        this(cart, null);
    }

    /**
     * Creates the order with the delivery address chosen by the buyer.
     * Without a chosen address, the buyer's main address is used.
     */
    public Order(Cart cart, Address deliveryAddress) {
        if (cart == null)
            throw new IllegalArgumentException("Cart is required.");
        if (cart.getItems().isEmpty())
            throw new IllegalArgumentException("Cannot create an order from an empty cart.");

        Address address = deliveryAddress == null ? cart.getBuyer().getMainAddress() : deliveryAddress;
        if (!cart.getBuyer().hasAddress(address))
            throw new IllegalArgumentException("The delivery address does not belong to the buyer.");

        this.id = UUID.randomUUID();
        this.deliveryAddress = address;
        this.buyer = cart.getBuyer();
        cart.getItems().forEach(
                i -> items.add(new OrderItem(i.getProduct(), i.getQuantity())));
        this.date = LocalDateTime.now();
        this.status = OrderStatus.PENDING_PAYMENT;
    }

    private Order(UUID id, Buyer buyer, List<OrderItem> items, LocalDateTime date,
                  Address deliveryAddress, OrderStatus status) {
        this.id = id;
        this.buyer = buyer;
        this.items.addAll(items);
        this.date = date;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
    }

    /** Rebuilds a order saved by a persistence adapter. It is not for creating new ones. */
    public static Order restore(UUID id, Buyer buyer, List<OrderItem> items, LocalDateTime date,
                                Address deliveryAddress, OrderStatus status) {
        Restore.require(id, "id");
        Restore.require(buyer, "buyer");
        Restore.require(items, "items");
        Restore.require(date, "date");
        Restore.require(deliveryAddress, "deliveryAddress");
        Restore.require(status, "status");
        if (items.isEmpty())
            throw new IllegalArgumentException("A stored order must have lines.");
        return new Order(id, buyer, items, date, deliveryAddress, status);
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
    public Address getDeliveryAddress() { return deliveryAddress; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
}