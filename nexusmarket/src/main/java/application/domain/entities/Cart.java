package application.domain.entities;

import application.domain.enums.ProductStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Cart {

    private final UUID id;
    private final Buyer buyer;
    private final List<CartItem> items = new ArrayList<>();

    public Cart(Buyer buyer) {
        if (buyer == null)
            throw new IllegalArgumentException("The cart must belong to a buyer.");

        this.id = UUID.randomUUID();
        this.buyer = buyer;
    }

    private Cart(UUID id, Buyer buyer, List<CartItem> items) {
        this.id = id;
        this.buyer = buyer;
        this.items.addAll(items);
    }

    /** Rebuilds a cart saved by a persistence adapter. It is not for creating new ones. */
    public static Cart restore(UUID id, Buyer buyer, List<CartItem> items) {
        Restore.require(id, "id");
        Restore.require(buyer, "buyer");
        Restore.require(items, "items");
        return new Cart(id, buyer, items);
    }

    public void addProduct(Product product, int quantity) {
        if (product == null)
            throw new IllegalArgumentException("Product is required.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        if (product.getStatus() != ProductStatus.PUBLISHED)
            throw new IllegalStateException("Only published products can be added.");

        items.stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        i -> i.increase(quantity),
                        () -> items.add(new CartItem(product, quantity)));
    }

    public void removeProduct(UUID productId) {
        boolean removed = items.removeIf(i -> i.getProduct().getId().equals(productId));
        if (!removed)
            throw new IllegalArgumentException("That product is not in the cart.");
    }

    public void clear() {
        items.clear();
    }

    public UUID getId() { return id; }
    public Buyer getBuyer() { return buyer; }
    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }
}