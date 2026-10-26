package application.domain.entities;

public class CartItem {

    private final Product product;
    private int quantity;

    CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    /** Rebuilds a cart line saved by a persistence adapter. */
    public static CartItem restore(Product product, int quantity) {
        Restore.require(product, "product");
        if (quantity <= 0)
            throw new IllegalArgumentException("A stored cart line must have units.");
        return new CartItem(product, quantity);
    }

    void increase(int units) {
        this.quantity += units;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
}