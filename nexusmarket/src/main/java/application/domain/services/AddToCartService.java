package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.CartItem;
import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.exceptions.AccessDeniedException;
import application.domain.ports.in.AddToCartUseCase;

import java.util.List;

public class AddToCartService implements AddToCartUseCase {

    private final BuyerValidationService buyerValidation;
    private final CheckAvailabilityService availability = new CheckAvailabilityService();

    public AddToCartService(BuyerValidationService buyerValidation) {
        if (buyerValidation == null)
            throw new IllegalArgumentException("Validation service is required.");
        this.buyerValidation = buyerValidation;
    }

    public void add(User user, Buyer buyer, Cart cart,
                        Product product, int quantity, List<Inventory> inventories) {
        buyerValidation.requireEnabled(user, buyer);
        if (cart == null)
            throw new IllegalArgumentException("Cart is required.");
        if (product == null)
            throw new IllegalArgumentException("Product is required.");
        if (inventories == null)
            throw new IllegalArgumentException("Inventory list is required.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero.");

        if (!cart.getBuyer().getId().equals(buyer.getId()))
            throw new AccessDeniedException("The cart belongs to another buyer.");
        if (!product.isPublished())
            throw new IllegalStateException("Only published products can be added.");

        int alreadyInCart = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .mapToInt(CartItem::getQuantity)
                .sum();

        // Adding to the cart does not reserve stock, but asking for more than exists is rejected.
        if (!availability.hasAvailability(product, alreadyInCart + quantity, inventories))
            throw new IllegalStateException(
                    "Not enough stock of " + product.getName() + ".");

        cart.addProduct(product, quantity);
    }
}