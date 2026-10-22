package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.ports.in.PublishProductUseCase;

import java.util.List;

public class PublishProductService implements PublishProductUseCase {

    private final AuthorizationService authorization;
    private final SellerValidationService sellerValidation;
    private final CheckAvailabilityService availability = new CheckAvailabilityService();

    public PublishProductService(AuthorizationService authorization,
                                      SellerValidationService sellerValidation) {
        if (authorization == null || sellerValidation == null)
            throw new IllegalArgumentException("Validation services are required.");
        this.authorization = authorization;
        this.sellerValidation = sellerValidation;
    }

    public void publish(User actor, Seller seller, Product product,
                         List<Inventory> inventories) {
        authorization.requirePermission(actor, BusinessProcess.PRODUCT_REGISTRATION);
        sellerValidation.requireOwner(actor, seller, product);
        if (inventories == null)
            throw new IllegalArgumentException("Inventory list is required.");

        // Physical products require stock; digital ones do not (they are delivered after payment).
        if (product.isPhysical() && !availability.hasAvailability(product, 1, inventories))
            throw new IllegalStateException(
                    "Cannot publish a physical product without available stock.");

        product.publish();
    }
}