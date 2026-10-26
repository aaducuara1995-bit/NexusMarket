package application.adapters.persistence.jpa.entities;

import application.domain.enums.CommercialStatus;

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

import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence entity (repository DTO) for Buyer, stored in MySQL table buyers.
 * Buyer profile. The main address is embedded; the extra ones live in buyer_addresses.
 * References to other aggregates are kept as identifiers; the adapter rebuilds them.
 */
@Entity
@Table(name = "buyers")
public class BuyerJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false, unique = true)
    private String userId;

    @Embedded
    private AddressEmbeddable mainAddress;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "buyer_addresses", joinColumns = @JoinColumn(name = "buyer_id"))
    @OrderColumn(name = "line_number")
    private List<AddressEmbeddable> additionalAddresses = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "commercial_status", length = 30, nullable = false)
    private CommercialStatus commercialStatus;

    public BuyerJpaEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public AddressEmbeddable getMainAddress() {
        return mainAddress;
    }

    public void setMainAddress(AddressEmbeddable mainAddress) {
        this.mainAddress = mainAddress;
    }

    public List<AddressEmbeddable> getAdditionalAddresses() {
        return additionalAddresses;
    }

    public void setAdditionalAddresses(List<AddressEmbeddable> additionalAddresses) {
        this.additionalAddresses = additionalAddresses;
    }

    public CommercialStatus getCommercialStatus() {
        return commercialStatus;
    }

    public void setCommercialStatus(CommercialStatus commercialStatus) {
        this.commercialStatus = commercialStatus;
    }
}