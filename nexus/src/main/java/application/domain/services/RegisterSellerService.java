package application.domain.services;

import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.enums.UserRole;
import application.domain.ports.in.RegisterSellerUseCase;
import application.domain.ports.out.SellerRepositoryPort;

public class RegisterSellerService implements RegisterSellerUseCase {

    private final AuthorizationService authorization;
    private final SellerRepositoryPort sellers;

    public RegisterSellerService(AuthorizationService authorization, SellerRepositoryPort sellers) {
        if (authorization == null || sellers == null)
            throw new IllegalArgumentException("Service dependencies are required.");
        this.authorization = authorization;
        this.sellers = sellers;
    }

    public Seller register(User admin, User sellerUser) {
        authorization.requirePermission(admin, BusinessProcess.SELLER_REGISTRATION);
        if (sellerUser == null)
            throw new IllegalArgumentException("The user to register is required.");
        if (!sellerUser.hasRole(UserRole.SELLER))
            throw new IllegalStateException("The user does not have the seller role.");
        if (!sellerUser.isActive())
            throw new IllegalStateException("A blocked user cannot be registered.");
        if (sellers.existsByUserId(sellerUser.getId()))
            throw new IllegalStateException("The user already has a seller profile.");

        Seller seller = new Seller(sellerUser.getId());
        sellers.save(seller);
        return seller;
    }
}