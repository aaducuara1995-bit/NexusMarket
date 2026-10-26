package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;

public class BuyerValidationService {

    public boolean canPurchase(User user, Buyer buyer) {
        validateInputs(user, buyer);

        return user.hasRole(UserRole.BUYER)
                && buyer.getUserId().equals(user.getId())
                && user.isActive()
                && buyer.canPurchase();
    }

    public void requireEnabled(User user, Buyer buyer) {
        validateInputs(user, buyer);

        if (!user.hasRole(UserRole.BUYER))
            throw new AccessDeniedException("The user does not have the buyer role.");
        if (!buyer.getUserId().equals(user.getId()))
            throw new AccessDeniedException("The buyer does not match this user.");
        if (!user.isActive())
            throw new IllegalStateException("The user is blocked and cannot purchase.");
        if (!buyer.canPurchase())
            throw new IllegalStateException("The buyer is restricted and cannot make purchases.");
    }

    private static void validateInputs(User user, Buyer buyer) {
        if (user == null)
            throw new IllegalArgumentException("User is required.");
        if (buyer == null)
            throw new IllegalArgumentException("Buyer is required.");
    }
}