package application.domain.ports.in;

import application.domain.entities.Buyer;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.AuthenticationResult;
import application.domain.valueobjects.Email;

import java.util.List;

/** Operations available without authentication: login, buyer sign-up and the public catalog. */
public interface PublicAccessPort {

    AuthenticationResult login(Email email, String password);

    Buyer registerBuyer(User user, Address mainAddress, String password);

    List<Product> consultCatalog();

    int consultAvailability(Product product);
}