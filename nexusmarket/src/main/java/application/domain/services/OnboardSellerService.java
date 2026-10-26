package application.domain.services;

import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.BusinessProcess;
import application.domain.enums.UserRole;
import application.domain.enums.WarehouseType;
import application.domain.valueobjects.Address;

public class OnboardSellerService {

    public record Result(Seller seller, Warehouse warehouse) {
    }

    private final AuthorizationService authorization;

    public OnboardSellerService(AuthorizationService authorization) {
        if (authorization == null)
            throw new IllegalArgumentException("Authorization service is required.");
        this.authorization = authorization;
    }

    public Result onboard(User admin,
                                User sellerUser,
                                String warehouseName,
                                WarehouseType warehouseType,
                                Address warehouseAddress) {
        // Sellers do not self-register: an admin onboards them,
        // together with their first warehouse (flow 6.1 of the specification).
        authorization.requirePermission(admin, BusinessProcess.SELLER_REGISTRATION);
        authorization.requirePermission(admin, BusinessProcess.WAREHOUSE_MANAGEMENT);

        if (sellerUser == null)
            throw new IllegalArgumentException("The user to onboard is required.");
        if (!sellerUser.hasRole(UserRole.SELLER))
            throw new IllegalStateException("Only a user with the seller role can be onboarded.");
        if (!sellerUser.isActive())
            throw new IllegalStateException("A blocked user cannot be onboarded.");

        Seller seller = new Seller(sellerUser.getId());
        Warehouse warehouse = Warehouse.ofSeller(seller.getId(), warehouseName, warehouseType, warehouseAddress);

        return new Result(seller, warehouse);
    }
}