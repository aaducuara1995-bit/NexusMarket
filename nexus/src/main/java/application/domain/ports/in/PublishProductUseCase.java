package application.domain.ports.in;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;

import java.util.List;

/** Input port: the seller publishes one of their products in the catalog. */
public interface PublishProductUseCase {

    void publish(User actor, Seller seller, Product product, List<Inventory> inventories);
}