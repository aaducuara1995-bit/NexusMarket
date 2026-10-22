package application.domain.ports.in;

import application.domain.entities.Seller;
import application.domain.entities.User;

/** Input port: an admin creates a user's seller profile. */
public interface RegisterSellerUseCase {

    Seller register(User admin, User sellerUser);
}