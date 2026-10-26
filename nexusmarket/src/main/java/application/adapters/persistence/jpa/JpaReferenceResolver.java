package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.mappers.BuyerJpaMapper;
import application.adapters.persistence.jpa.mappers.InventoryJpaMapper;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaBuyerRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInventoryRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.adapters.persistence.jpa.repositories.SpringDataJpaWarehouseRepository;
import application.domain.entities.Buyer;
import application.domain.entities.Inventory;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.Warehouse;
import org.springframework.stereotype.Component;

/**
 * Rebuilds the domain objects that other aggregates reference by id. The tables only
 * store identifiers (an inventory keeps product_id and warehouse_id, an invoice keeps
 * order_id), so the adapters ask this class for the full domain object when reading.
 */
@Component
public class JpaReferenceResolver {

    private final SpringDataJpaBuyerRepository buyers;
    private final SpringDataJpaProductRepository products;
    private final SpringDataJpaWarehouseRepository warehouses;
    private final SpringDataJpaInventoryRepository inventories;
    private final SpringDataJpaOrderRepository orders;

    public JpaReferenceResolver(SpringDataJpaBuyerRepository buyers,
                                SpringDataJpaProductRepository products,
                                SpringDataJpaWarehouseRepository warehouses,
                                SpringDataJpaInventoryRepository inventories,
                                SpringDataJpaOrderRepository orders) {
        this.buyers = buyers;
        this.products = products;
        this.warehouses = warehouses;
        this.inventories = inventories;
        this.orders = orders;
    }

    public Buyer buyer(String id) {
        return buyers.findById(id).map(BuyerJpaMapper::toDomain)
                .orElseThrow(() -> missing("buyer", id));
    }

    public Product product(String id) {
        return products.findById(id).map(ProductJpaMapper::toDomain)
                .orElseThrow(() -> missing("product", id));
    }

    public Warehouse warehouse(String id) {
        return warehouses.findById(id).map(WarehouseJpaMapper::toDomain)
                .orElseThrow(() -> missing("warehouse", id));
    }

    public Inventory inventory(String id) {
        return inventories.findById(id).map(this::inventory)
                .orElseThrow(() -> missing("inventory", id));
    }

    public Inventory inventory(InventoryJpaEntity entity) {
        return InventoryJpaMapper.toDomain(entity, product(entity.getProductId()),
                warehouse(entity.getWarehouseId()));
    }

    public Order order(String id) {
        return orders.findById(id).map(this::order)
                .orElseThrow(() -> missing("order", id));
    }

    public Order order(OrderJpaEntity entity) {
        return OrderJpaMapper.toDomain(entity, buyer(entity.getBuyerId()));
    }

    private static IllegalStateException missing(String what, String id) {
        return new IllegalStateException("Stored data references a missing " + what + ": " + id);
    }
}