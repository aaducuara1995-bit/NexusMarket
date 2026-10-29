package application.adapters.rest;

import application.domain.entities.Inventory;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Turns the identifiers that arrive in a URL into domain objects for the input ports.
 *
 * The reference project builds "reference" models with setters (only the id filled in).
 * NexusMarket entities protect their invariants and have no setters, so an object with
 * only an id cannot exist. This loader reads the object through the output ports
 * instead; the use case then reloads it inside its transaction, as it always does.
 * It never changes data.
 */
@Component
public class RestReferenceLoader {

    private final UserRepositoryPort users;
    private final SellerRepositoryPort sellers;
    private final WarehouseRepositoryPort warehouses;
    private final ProductRepositoryPort products;
    private final InventoryRepositoryPort inventories;
    private final OrderRepositoryPort orders;
    private final ReturnRequestRepositoryPort returns;
    private final RefundRepositoryPort refunds;

    public RestReferenceLoader(UserRepositoryPort users, SellerRepositoryPort sellers,
                               WarehouseRepositoryPort warehouses, ProductRepositoryPort products,
                               InventoryRepositoryPort inventories, OrderRepositoryPort orders,
                               ReturnRequestRepositoryPort returns, RefundRepositoryPort refunds) {
        this.users = users;
        this.sellers = sellers;
        this.warehouses = warehouses;
        this.products = products;
        this.inventories = inventories;
        this.orders = orders;
        this.returns = returns;
        this.refunds = refunds;
    }

    public User user(UUID id) {
        return found(users.findById(id), "User");
    }

    public Seller seller(UUID id) {
        return found(sellers.findById(id), "Seller");
    }

    public Warehouse warehouse(UUID id) {
        return found(warehouses.findById(id), "Warehouse");
    }

    public Product product(UUID id) {
        return found(products.findById(id), "Product");
    }

    public Inventory inventory(UUID id) {
        return found(inventories.findById(id), "Inventory");
    }

    public Order order(UUID id) {
        return found(orders.findById(id), "Order");
    }

    public ReturnRequest returnRequest(UUID id) {
        return found(returns.findById(id), "Return request");
    }

    public Refund refund(UUID id) {
        return found(refunds.findById(id), "Refund");
    }

    public Optional<Refund> refundOfOrder(UUID orderId) {
        return refunds.findByOrder(orderId);
    }

    public List<Warehouse> warehousesOfSeller(UUID sellerId) {
        return warehouses.findAll().stream().filter(w -> w.belongsTo(sellerId)).toList();
    }

    private static <T> T found(Optional<T> value, String entity) {
        return value.orElseThrow(() -> new EntityNotFoundException(entity));
    }
}