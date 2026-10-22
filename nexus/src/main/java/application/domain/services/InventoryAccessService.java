package application.domain.services;

import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.enums.UserRole;

public class InventoryAccessService {

    private final AuthorizationService authorization;
    private final SellerValidationService sellerValidation;

    public InventoryAccessService(AuthorizationService authorization,
                                   SellerValidationService sellerValidation) {
        if (authorization == null || sellerValidation == null)
            throw new IllegalArgumentException("Validation services are required.");
        this.authorization = authorization;
        this.sellerValidation = sellerValidation;
    }

    /**
     * A seller only manages the inventory of their own products.
     * The logistic operator runs the warehouses and does not need to own the product.
     */
    public void requireAccess(User actor, Seller seller, Product product) {
        authorization.requirePermission(actor, BusinessProcess.INVENTORY_MANAGEMENT);
        if (product == null)
            throw new IllegalArgumentException("Product is required.");

        if (actor.hasRole(UserRole.SELLER))
            sellerValidation.requireOwner(actor, seller, product);
    }
}