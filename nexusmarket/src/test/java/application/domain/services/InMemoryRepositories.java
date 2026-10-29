package application.domain.services;

import application.domain.entities.AuditLog;
import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Inventory;
import application.domain.entities.InventoryMovement;
import application.domain.entities.InventoryReservation;
import application.domain.entities.Invoice;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.Shipment;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.ports.out.AuditLogRepositoryPort;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.CartRepositoryPort;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.InventoryReservationRepositoryPort;
import application.domain.ports.out.InvoiceRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.ShipmentRepositoryPort;
import application.domain.ports.out.TokenServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/** Simple in-memory implementations of the output ports, for tests only. */
public final class InMemoryRepositories {

    private InMemoryRepositories() {
    }

    /** Keeps one copy per identifier: saving an existing object replaces it. */
    public abstract static class Store<T> {
        public final List<T> saved = new ArrayList<>();

        protected abstract UUID idOf(T item);

        public void save(T item) {
            saved.removeIf(existing -> idOf(existing).equals(idOf(item)));
            saved.add(item);
        }

        public void saveAll(List<T> items) {
            items.forEach(this::save);
        }

        public List<T> findAll() {
            return List.copyOf(saved);
        }

        public Optional<T> findById(UUID id) {
            return first(item -> idOf(item).equals(id));
        }

        protected Optional<T> first(Predicate<T> condition) {
            return saved.stream().filter(condition).findFirst();
        }

        protected List<T> where(Predicate<T> condition) {
            return saved.stream().filter(condition).toList();
        }
    }

    public static class Users extends Store<User> implements UserRepositoryPort {
        protected UUID idOf(User user) { return user.getId(); }

        public boolean existsByEmail(Email email) {
            return first(u -> u.getEmail().equals(email)).isPresent();
        }

        public boolean existsByDocument(IdentityDocument document) {
            return first(u -> u.getDocument().equals(document)).isPresent();
        }

        public Optional<User> findByEmail(Email email) {
            return first(u -> u.getEmail().equals(email));
        }
    }

    public static class Buyers extends Store<Buyer> implements BuyerRepositoryPort {
        protected UUID idOf(Buyer buyer) { return buyer.getId(); }

        public boolean existsByUserId(UUID userId) {
            return findByUserId(userId).isPresent();
        }

        public Optional<Buyer> findByUserId(UUID userId) {
            return first(b -> b.getUserId().equals(userId));
        }
    }

    public static class Sellers extends Store<Seller> implements SellerRepositoryPort {
        protected UUID idOf(Seller seller) { return seller.getId(); }

        public boolean existsByUserId(UUID userId) {
            return findByUserId(userId).isPresent();
        }

        public Optional<Seller> findByUserId(UUID userId) {
            return first(s -> s.getUserId().equals(userId));
        }
    }

    public static class Warehouses extends Store<Warehouse> implements WarehouseRepositoryPort {
        protected UUID idOf(Warehouse warehouse) { return warehouse.getId(); }

        public boolean existsByName(String name) {
            return first(w -> w.getName().equalsIgnoreCase(name)).isPresent();
        }
    }

    public static class Products extends Store<Product> implements ProductRepositoryPort {
        protected UUID idOf(Product product) { return product.getId(); }

        public boolean existsNameForSeller(UUID sellerId, String name) {
            return first(p -> p.getSellerId().equals(sellerId) && p.getName().equalsIgnoreCase(name)).isPresent();
        }

        public List<Product> findBySeller(UUID sellerId) {
            return where(p -> p.getSellerId().equals(sellerId));
        }

        public List<Product> findPublished() {
            return where(Product::isPublished);
        }
    }

    public static class Inventories extends Store<Inventory> implements InventoryRepositoryPort {
        protected UUID idOf(Inventory inventory) { return inventory.getId(); }

        public List<Inventory> findByProduct(UUID productId) {
            return where(i -> i.getProduct().getId().equals(productId));
        }
    }

    public static class Reservations extends Store<InventoryReservation> implements InventoryReservationRepositoryPort {
        protected UUID idOf(InventoryReservation reservation) { return reservation.getId(); }

        public List<InventoryReservation> findByOrder(UUID orderId) {
            return where(r -> r.belongsToOrder(orderId));
        }
    }

    public static class Movements extends Store<InventoryMovement> implements InventoryMovementRepositoryPort {
        protected UUID idOf(InventoryMovement movement) { return movement.getId(); }

        public List<InventoryMovement> findByInventory(UUID inventoryId) {
            return where(m -> m.getInventoryId().equals(inventoryId));
        }
    }

    public static class Carts extends Store<Cart> implements CartRepositoryPort {
        protected UUID idOf(Cart cart) { return cart.getId(); }

        public Optional<Cart> findByBuyer(UUID buyerId) {
            return first(c -> c.getBuyer().getId().equals(buyerId));
        }
    }

    public static class Orders extends Store<Order> implements OrderRepositoryPort {
        protected UUID idOf(Order order) { return order.getId(); }

        public List<Order> findByBuyer(UUID buyerId) {
            return where(o -> o.belongsTo(buyerId));
        }
    }

    public static class Invoices extends Store<Invoice> implements InvoiceRepositoryPort {
        protected UUID idOf(Invoice invoice) { return invoice.getId(); }

        public Optional<Invoice> findByOrder(UUID orderId) {
            return first(i -> i.getOrder().getId().equals(orderId));
        }
    }

    public static class Shipments extends Store<Shipment> implements ShipmentRepositoryPort {
        protected UUID idOf(Shipment shipment) { return shipment.getId(); }

        public Optional<Shipment> findByOrder(UUID orderId) {
            return first(s -> s.getOrder().getId().equals(orderId));
        }
    }

    public static class Returns extends Store<ReturnRequest> implements ReturnRequestRepositoryPort {
        protected UUID idOf(ReturnRequest returnRequest) { return returnRequest.getId(); }

        public List<ReturnRequest> findByOrder(UUID orderId) {
            return where(r -> r.getOrder().getId().equals(orderId));
        }
    }

    public static class Refunds extends Store<Refund> implements RefundRepositoryPort {
        protected UUID idOf(Refund refund) { return refund.getId(); }

        public Optional<Refund> findByOrder(UUID orderId) {
            return first(r -> r.getOrder().getId().equals(orderId));
        }
    }

    public static class AuditLogs extends Store<AuditLog> implements AuditLogRepositoryPort {
        protected UUID idOf(AuditLog auditLog) { return auditLog.getId(); }

        public List<AuditLog> findByUser(UUID userId) {
            return where(a -> a.getPerformedByUserId().equals(userId));
        }

        public List<AuditLog> findByAffectedEntity(UUID entityId) {
            return where(a -> a.getAffectedEntityId().equals(entityId));
        }
    }

    /** Readable fake hashing for tests: "hash:" + password. */
    public static class FakePasswords implements PasswordServicePort {
        public String encrypt(String rawPassword) { return "hash:" + rawPassword; }

        public boolean matches(String rawPassword, String passwordHash) {
            return passwordHash != null && passwordHash.equals("hash:" + rawPassword);
        }
    }

    /** Fake token: "token-" + user id. */
    public static class FakeTokens implements TokenServicePort {
        public String generateToken(User user) { return "token-" + user.getId(); }
    }
}