package application.domain.services;

import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;

public class SellerValidationService {

    public void requireSeller(User user, Seller seller) {
        if (user == null)
            throw new IllegalArgumentException("User is required.");
        if (seller == null)
            throw new IllegalArgumentException("Seller is required.");

        if (!user.hasRole(UserRole.SELLER))
            throw new AccessDeniedException("The user does not have the seller role.");
        if (!seller.getUserId().equals(user.getId()))
            throw new AccessDeniedException("The seller does not match this user.");
        if (!user.isActive())
            throw new IllegalStateException("The user is blocked and cannot operate.");
    }

    public void requireOwner(User user, Seller seller, Product product) {
        requireSeller(user, seller);

        if (product == null)
            throw new IllegalArgumentException("Product is required.");
        if (!product.belongsTo(seller.getId()))
            throw new AccessDeniedException("The product belongs to another seller.");
    }
}