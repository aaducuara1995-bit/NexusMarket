package application.adapters.persistence.jpa.entities;

import application.domain.enums.ProductType;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

/** One line of an order with the product data copied at purchase time. */
@Embeddable
public class OrderItemEmbeddable {

    @Column(name = "product_id", length = 36, nullable = false)
    private String productId;

    @Column(name = "product_name", length = 120, nullable = false)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private ProductType type;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPriceAmount;

    @Column(name = "unit_price_currency", length = 3, nullable = false)
    private String unitPriceCurrency;

    public OrderItemEmbeddable() {
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPriceAmount() {
        return unitPriceAmount;
    }

    public void setUnitPriceAmount(BigDecimal unitPriceAmount) {
        this.unitPriceAmount = unitPriceAmount;
    }

    public String getUnitPriceCurrency() {
        return unitPriceCurrency;
    }

    public void setUnitPriceCurrency(String unitPriceCurrency) {
        this.unitPriceCurrency = unitPriceCurrency;
    }
}