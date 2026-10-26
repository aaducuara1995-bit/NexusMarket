package application.adapters.persistence.jpa.entities;

import application.domain.enums.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA persistence entity (repository DTO) for InventoryReservation, stored in MySQL table inventory_reservations.
 * Units set aside in one inventory for one order.
 * References to other aggregates are kept as identifiers; the adapter rebuilds them.
 */
@Entity
@Table(name = "inventory_reservations")
public class InventoryReservationJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "order_id", length = 36, nullable = false)
    private String orderId;

    @Column(name = "inventory_id", length = 36, nullable = false)
    private String inventoryId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private ReservationStatus status;

    public InventoryReservationJpaEntity() {
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

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}