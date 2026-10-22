package application.domain.ports.in;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;

import java.util.List;

/** Input port: the seller suspends or discontinues one of their products. */
public interface ChangeProductStatusUseCase {

    void suspend(User actor, Seller seller, Product product);

    void discontinue(User actor, Seller seller, Product product, List<Inventory> inventories);
}