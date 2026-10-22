package application.domain.services;

import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.BusinessProcess;
import application.domain.enums.WarehouseType;
import application.domain.ports.in.RegisterWarehouseUseCase;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.valueobjects.Address;

public class RegisterWarehouseService implements RegisterWarehouseUseCase {

    private final AuthorizationService authorization;
    private final WarehouseRepositoryPort warehouses;

    public RegisterWarehouseService(AuthorizationService authorization, WarehouseRepositoryPort warehouses) {
        if (authorization == null || warehouses == null)
            throw new IllegalArgumentException("Service dependencies are required.");
        this.authorization = authorization;
        this.warehouses = warehouses;
    }

    public Warehouse registerForMarketplace(User admin, String name,
                                          WarehouseType type, Address address) {
        validate(admin, name);
        Warehouse warehouse = Warehouse.ofMarketplace(name, type, address);
        warehouses.save(warehouse);
        return warehouse;
    }

    public Warehouse registerForSeller(User admin, Seller seller, String name,
                                      WarehouseType type, Address address) {
        validate(admin, name);
        if (seller == null)
            throw new IllegalArgumentException("Seller is required.");

        Warehouse warehouse = Warehouse.ofSeller(seller.getId(), name, type, address);
        warehouses.save(warehouse);
        return warehouse;
    }

    private void validate(User admin, String name) {
        authorization.requirePermission(admin, BusinessProcess.WAREHOUSE_MANAGEMENT);
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Warehouse name cannot be empty.");
        if (warehouses.existsByName(name.trim()))
            throw new IllegalStateException("A warehouse with that name already exists.");
    }
}