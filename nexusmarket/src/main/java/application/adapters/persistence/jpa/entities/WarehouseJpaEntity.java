package application.adapters.persistence.jpa.entities;

import application.domain.enums.WarehouseOwnership;
import application.domain.enums.WarehouseType;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA persistence entity (repository DTO) for Warehouse, stored in MySQL table warehouses.
 * Marketplace or seller warehouse. Digital warehouses have no address.
 * References to other aggregates are kept as identifiers; the adapter rebuilds them.
 */
@Entity
@Table(name = "warehouses")
public class WarehouseJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", length = 120, nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private WarehouseType type;

    @Embedded
    private AddressEmbeddable address;

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership", length = 30, nullable = false)
    private WarehouseOwnership ownership;

    @Column(name = "seller_id", length = 36)
    private String sellerId;

    public WarehouseJpaEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WarehouseType getType() {
        return type;
    }

    public void setType(WarehouseType type) {
        this.type = type;
    }

    public AddressEmbeddable getAddress() {
        return address;
    }

    public void setAddress(AddressEmbeddable address) {
        this.address = address;
    }

    public WarehouseOwnership getOwnership() {
        return ownership;
    }

    public void setOwnership(WarehouseOwnership ownership) {
        this.ownership = ownership;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
}