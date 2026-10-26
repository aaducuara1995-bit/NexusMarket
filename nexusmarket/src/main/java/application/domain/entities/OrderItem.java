package application.domain.entities;

import application.domain.enums.ProductType;
import application.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {

    private final UUID productId;
    private final String productName;
    private final ProductType type;
    private final int quantity;
    private final Money unitPrice;

    OrderItem(Product product, int quantity) {
        this.productId = product.getId();
        this.productName = product.getName();
        this.type = product.getType();
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }

    private OrderItem(UUID productId, String productName, ProductType type,
                      int quantity, Money unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.type = type;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /** Rebuilds an order line saved by a persistence adapter. */
    public static OrderItem restore(UUID productId, String productName, ProductType type,
                                    int quantity, Money unitPrice) {
        Restore.require(productId, "productId");
        Restore.require(productName, "productName");
        Restore.require(type, "type");
        Restore.require(unitPrice, "unitPrice");
        if (quantity <= 0)
            throw new IllegalArgumentException("A stored order line must have units.");
        return new OrderItem(productId, productName, type, quantity, unitPrice);
    }

    public Money subtotal() {
        return new Money(
                unitPrice.amount().multiply(BigDecimal.valueOf(quantity)),
                unitPrice.currency());
    }

    public boolean isPhysical() {
        return type == ProductType.PHYSICAL;
    }

    public UUID getProductId() { return productId; }
    public String getProductName() { return productName; }
    public ProductType getType() { return type; }
    public int getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }
}