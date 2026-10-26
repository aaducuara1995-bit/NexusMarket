package application.domain.ports.in;

import application.domain.entities.Inventory;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.ReturnRequest;
import application.domain.entities.User;
import application.domain.entities.Warehouse;

import java.util.List;

/** Operations of the LOGISTIC_OPERATOR role: warehouse operation and shipping. */
public interface LogisticOperatorPort {

    Inventory registerInitialInventory(User user, Product product, Warehouse warehouse, int quantity);

    Inventory adjustInventory(User user, Inventory inventory, int difference, String reason);

    Inventory markInventoryDamaged(User user, Inventory inventory);

    Inventory blockInventory(User user, Inventory inventory);

    Inventory enableInventory(User user, Inventory inventory);

    Inventory restockFromReturn(User user, Inventory inventory, ReturnRequest returnRequest, int units);

    List<Order> consultOrdersToShip(User user);

    Order shipOrder(User user, Order order);

    Order confirmDelivery(User user, Order order);
}