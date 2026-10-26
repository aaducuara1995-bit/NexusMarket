package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Inventory;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.enums.WarehouseType;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;
import application.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.util.UUID;

final class Fixtures {

    private Fixtures() {
    }

    static Address address() {
        return new Address("Calle 1 # 2-3", "Medellin", "Antioquia", "050001");
    }

    static User user(UserRole role) {
        return new User(
                new FullName("Ana", "Perez"),
                new Email("ana@mail.com"),
                new IdentityDocument("123456"),
                role);
    }

    static Seller seller(User user) {
        return new Seller(user.getId());
    }

    static Buyer buyer(User user) {
        return new Buyer(user.getId(), address());
    }

    static Product product(ProductType type) {
        return product(UUID.randomUUID(), type);
    }

    static Product product(UUID sellerId, ProductType type) {
        return new Product(sellerId, "T-shirt", "Cotton", type,
                new Money(new BigDecimal("45000")));
    }

    static Warehouse warehouse() {
        return Warehouse.ofMarketplace("Central warehouse", WarehouseType.PHYSICAL, address());
    }

    static Inventory inventory(Product product, int quantity) {
        return new Inventory(product, warehouse(), quantity);
    }

    static Order deliveredOrder(Product product, int quantity) {
        if (!product.isPublished())
            product.publish();

        Cart cart = new Cart(buyer(user(UserRole.BUYER)));
        cart.addProduct(product, quantity);

        Order order = new Order(cart);
        order.pay();
        order.ship();
        order.complete();
        return order;
    }
}