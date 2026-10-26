package application.adapters.persistence.jpa.entities;

import application.domain.enums.ReturnStatus;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

import java.util.HashMap;
import java.util.Map;

/**
 * JPA persistence entity (repository DTO) for ReturnRequest, stored in MySQL table return_requests.
 * Return request. Restocked units per product live in return_restocked_units.
 * References to other aggregates are kept as identifiers; the adapter rebuilds them.
 */
@Entity
@Table(name = "return_requests")
public class ReturnRequestJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "order_id", length = 36, nullable = false)
    private String orderId;

    @Column(name = "reason", length = 500, nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private ReturnStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "return_restocked_units", joinColumns = @JoinColumn(name = "return_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "units", nullable = false)
    private Map<String, Integer> restockedUnits = new HashMap<>();

    public ReturnRequestJpaEntity() {
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ReturnStatus getStatus() {
        return status;
    }

    public void setStatus(ReturnStatus status) {
        this.status = status;
    }

    public Map<String, Integer> getRestockedUnits() {
        return restockedUnits;
    }

    public void setRestockedUnits(Map<String, Integer> restockedUnits) {
        this.restockedUnits = restockedUnits;
    }
}