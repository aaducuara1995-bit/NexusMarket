package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.BusinessProcess;

import java.util.List;

public class ChangeProductStatusService {

    private final AuthorizationService authorization;
    private final SellerValidationService sellerValidation;

    public ChangeProductStatusService(AuthorizationService authorization,
                                       SellerValidationService sellerValidation) {
        if (authorization == null || sellerValidation == null)
            throw new IllegalArgumentException("Validation services are required.");
        this.authorization = authorization;
        this.sellerValidation = sellerValidation;
    }

    public void suspend(User actor, Seller seller, Product product) {
        authorization.requirePermission(actor, BusinessProcess.PRODUCT_REGISTRATION);
        sellerValidation.requireOwner(actor, seller, product);

        product.suspend();
    }

    public void discontinue(User actor, Seller seller, Product product,
                             List<Inventory> inventories) {
        authorization.requirePermission(actor, BusinessProcess.PRODUCT_REGISTRATION);
        sellerValidation.requireOwner(actor, seller, product);
        if (inventories == null)
            throw new IllegalArgumentException("Inventory list is required.");

        boolean hasActiveReservations = inventories.stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .anyMatch(i -> i.getReserved() > 0);

        if (hasActiveReservations)
            throw new IllegalStateException(
                    "Cannot discontinue a product with active reservations in ongoing orders.");

        product.discontinue();
    }
}