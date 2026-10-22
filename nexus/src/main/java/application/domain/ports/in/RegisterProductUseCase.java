package application.domain.ports.in;

import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.ProductType;
import application.domain.valueobjects.Money;

/** Input port: the seller registers a new product, which starts suspended. */
public interface RegisterProductUseCase {

    Product register(User actor, Seller seller, String name, String description,
                       ProductType type, Money price);
}