package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.valueobjects.Address;

public class RegisterBuyerService {

    private final BuyerRepositoryPort buyers;

    public RegisterBuyerService(BuyerRepositoryPort buyers) {
        if (buyers == null)
            throw new IllegalArgumentException("Buyer repository is required.");
        this.buyers = buyers;
    }

    public Buyer register(User user, Address mainAddress) {
        if (user == null)
            throw new IllegalArgumentException("User is required.");
        if (!user.hasRole(UserRole.BUYER))
            throw new IllegalStateException("The user does not have the buyer role.");
        if (!user.isActive())
            throw new IllegalStateException("A blocked user cannot register as a buyer.");
        if (buyers.existsByUserId(user.getId()))
            throw new IllegalStateException("The user already has a buyer profile.");

        Buyer buyer = new Buyer(user.getId(), mainAddress);
        buyers.save(buyer);
        return buyer;
    }
}