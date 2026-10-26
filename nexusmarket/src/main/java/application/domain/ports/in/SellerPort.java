package application.domain.ports.in;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.ReturnRequest;
import application.domain.entities.User;
import application.domain.entities.Warehouse;

import java.util.List;

/** Operations of the SELLER role: catalog and inventory of their own products. */
public interface SellerPort {

    Product registerProduct(User user, Product product);

    Product publishProduct(User user, Product product);

    Product suspendProduct(User user, Product product);

    Product discontinueProduct(User user, Product product);

    List<Product> consultMyProducts(User user);

    Inventory registerInitialInventory(User user, Product product, Warehouse warehouse, int quantity);

    Inventory adjustInventory(User user, Inventory inventory, int difference, String reason);

    Inventory markInventoryDamaged(User user, Inventory inventory);

    Inventory blockInventory(User user, Inventory inventory);

    Inventory enableInventory(User user, Inventory inventory);

    Inventory restockFromReturn(User user, Inventory inventory, ReturnRequest returnRequest, int units);
}