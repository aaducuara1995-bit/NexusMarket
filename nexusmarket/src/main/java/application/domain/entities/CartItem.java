package application.domain.entities;

public class CartItem {

    private final Product product;
    private int quantity;

    CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    void increase(int units) {
        this.quantity += units;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
}