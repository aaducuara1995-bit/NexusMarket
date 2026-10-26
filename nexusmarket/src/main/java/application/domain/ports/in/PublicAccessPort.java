package application.domain.ports.in;

import application.domain.entities.Buyer;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.valueobjects.Address;

import java.util.List;

/** Operations available without authentication: buyer sign-up and the public catalog. */
public interface PublicAccessPort {

    Buyer registerBuyer(User user, Address mainAddress);

    List<Product> consultCatalog();

    int consultAvailability(Product product);
}