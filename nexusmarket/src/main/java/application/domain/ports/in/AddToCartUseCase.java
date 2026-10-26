package application.domain.ports.in;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.User;

import java.util.List;

/** Input port: the buyer adds a published product to their cart. */
public interface AddToCartUseCase {

    void add(User user, Buyer buyer, Cart cart,
                 Product product, int quantity, List<Inventory> inventories);
}