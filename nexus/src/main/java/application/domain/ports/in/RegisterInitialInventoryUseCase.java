package application.domain.ports.in;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.services.RegisterInitialInventoryService;

import java.util.List;

/** Input port: registers a product's initial stock in a warehouse. */
public interface RegisterInitialInventoryUseCase {

    RegisterInitialInventoryService.Result register(User actor, Seller seller, Product product,
                                                         Warehouse warehouse, int initialQuantity,
                                                         List<Inventory> existing);
}