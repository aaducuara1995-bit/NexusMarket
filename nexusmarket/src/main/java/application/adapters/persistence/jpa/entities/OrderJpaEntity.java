package application.adapters.persistence.jpa.entities;

import application.domain.enums.OrderStatus;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence entity (repository DTO) for Order, stored in MySQL table orders.
 * Customer order. Lines live in order_items with a copy of the product data.
 * References to other aggregates are kept as identifiers; the adapter rebuilds them.
 */
@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "buyer_id", length = 36, nullable = false)
    private String buyerId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime date;

    @Embedded
    private AddressEmbeddable deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private OrderStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @OrderColumn(name = "line_number")
    private List<OrderItemEmbeddable> items = new ArrayList<>();

    public OrderJpaEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public AddressEmbeddable getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(AddressEmbeddable deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderItemEmbeddable> getItems() {
        return items;
    }

    public void setItems(List<OrderItemEmbeddable> items) {
        this.items = items;
    }
}