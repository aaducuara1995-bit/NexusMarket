package application.domain.ports.in;

import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.WarehouseType;
import application.domain.valueobjects.Address;

/** Input port: an admin registers a Marketplace or seller warehouse. */
public interface RegisterWarehouseUseCase {

    Warehouse registerForMarketplace(User admin, String name, WarehouseType type, Address address);

    Warehouse registerForSeller(User admin, Seller seller, String name,
                               WarehouseType type, Address address);
}