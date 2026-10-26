package application.adapters.persistence.jpa.entities;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence entity (repository DTO) for Product, stored in MySQL table products.
 * Catalog product. Variants live in product_variants.
 * References to other aggregates are kept as identifiers; the adapter rebuilds them.
 */
@Entity
@Table(name = "products")
public class ProductJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "seller_id", length = 36, nullable = false)
    private String sellerId;

    @Column(name = "name", length = 120, nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private ProductType type;

    @Column(name = "price_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal priceAmount;

    @Column(name = "price_currency", length = 3, nullable = false)
    private String priceCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private ProductStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_variants", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "line_number")
    @Column(name = "variant", length = 80, nullable = false)
    private List<String> variants = new ArrayList<>();

    public ProductJpaEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public BigDecimal getPriceAmount() {
        return priceAmount;
    }

    public void setPriceAmount(BigDecimal priceAmount) {
        this.priceAmount = priceAmount;
    }

    public String getPriceCurrency() {
        return priceCurrency;
    }

    public void setPriceCurrency(String priceCurrency) {
        this.priceCurrency = priceCurrency;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public List<String> getVariants() {
        return variants;
    }

    public void setVariants(List<String> variants) {
        this.variants = variants;
    }
}