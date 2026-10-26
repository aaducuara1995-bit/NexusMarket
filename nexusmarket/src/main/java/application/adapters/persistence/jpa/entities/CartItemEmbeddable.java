package application.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** One line of a cart: product and units. */
@Embeddable
public class CartItemEmbeddable {

    @Column(name = "product_id", length = 36, nullable = false)
    private String productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public CartItemEmbeddable() {
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}