package application.domain.services;

import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.enums.ProductType;
import application.domain.ports.in.RegisterProductUseCase;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.valueobjects.Money;

public class RegisterProductService implements RegisterProductUseCase {

    private final AuthorizationService authorization;
    private final SellerValidationService sellerValidation;
    private final ProductRepositoryPort products;

    public RegisterProductService(AuthorizationService authorization,
                                   SellerValidationService sellerValidation,
                                   ProductRepositoryPort products) {
        if (authorization == null || sellerValidation == null || products == null)
            throw new IllegalArgumentException("Service dependencies are required.");
        this.authorization = authorization;
        this.sellerValidation = sellerValidation;
        this.products = products;
    }

    public Product register(User actor, Seller seller, String name, String description,
                              ProductType type, Money price) {
        authorization.requirePermission(actor, BusinessProcess.PRODUCT_REGISTRATION);
        sellerValidation.requireSeller(actor, seller);

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be empty.");
        if (products.existsNameForSeller(seller.getId(), name.trim()))
            throw new IllegalStateException("The seller already has a product with that name.");

        // The product starts suspended; it is published later with PublishProductService.
        Product product = new Product(seller.getId(), name.trim(), description, type, price);
        products.save(product);
        return product;
    }
}