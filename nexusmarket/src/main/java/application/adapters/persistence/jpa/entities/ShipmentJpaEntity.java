package application.adapters.persistence.jpa.entities;

import application.domain.enums.ShipmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA persistence entity (repository DTO) for Shipment, stored in MySQL table shipments.
 * Shipment of an order with physical products.
 * References to other aggregates are kept as identifiers; the adapter rebuilds them.
 */
@Entity
@Table(name = "shipments")
public class ShipmentJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "order_id", length = 36, nullable = false, unique = true)
    private String orderId;

    @Embedded
    private AddressEmbeddable deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private ShipmentStatus status;

    public ShipmentJpaEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public AddressEmbeddable getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(AddressEmbeddable deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }
}