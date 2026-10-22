package application.domain.ports.in;

import application.domain.entities.Buyer;
import application.domain.entities.User;
import application.domain.valueobjects.Address;

/** Input port: creates a user's buyer profile. */
public interface RegisterBuyerUseCase {

    Buyer register(User user, Address mainAddress);
}