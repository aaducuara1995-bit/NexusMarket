package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.Shipment;
import application.domain.entities.User;
import application.domain.enums.ProductType;
import application.domain.enums.ShipmentStatus;
import application.domain.enums.UserRole;
import application.domain.valueobjects.Address;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateShipmentServiceTest {

    private final CreateShipmentService service = new CreateShipmentService();

    private final User user = Fixtures.user(UserRole.BUYER);
    private final Buyer buyer = Fixtures.buyer(user);

    private Order order(ProductType type, boolean paid) {
        Product product = Fixtures.product(type);
        product.publish();
        Cart cart = new Cart(buyer);
        cart.addProduct(product, 1);
        Order order = new Order(cart);
        if (paid)
            order.pay();
        return order;
    }

    @Test
    void createsTheShipmentOfAPaidPhysicalOrder() {
        Order order = order(ProductType.PHYSICAL, true);

        Shipment shipment = service.create(order, buyer.getMainAddress(), List.of());

        assertEquals(ShipmentStatus.PENDING, shipment.getStatus());
        assertEquals(order.getId(), shipment.getOrder().getId());
    }

    @Test
    void acceptsAnAdditionalAddressOfTheBuyer() {
        Address other = new Address("Carrera 5 # 6-7", "Bello", "Antioquia", "051050");
        buyer.addAdditionalAddress(other);
        Order order = order(ProductType.PHYSICAL, true);

        Shipment shipment = service.create(order, other, List.of());

        assertEquals(other, shipment.getDeliveryAddress());
    }

    @Test
    void rejectsAnAddressNotOwnedByTheBuyer() {
        Address foreign = new Address("Calle 99 # 1-1", "Cali", "Valle", "760001");
        Order order = order(ProductType.PHYSICAL, true);

        assertThrows(IllegalArgumentException.class,
                () -> service.create(order, foreign, List.of()));
    }

    @Test
    void digitalOnlyOrdersHaveNoShipment() {
        Order order = order(ProductType.DIGITAL, true);

        assertThrows(IllegalStateException.class,
                () -> service.create(order, buyer.getMainAddress(), List.of()));
    }

    @Test
    void rejectsShipmentOfAnUnpaidOrder() {
        Order order = order(ProductType.PHYSICAL, false);

        assertThrows(IllegalStateException.class,
                () -> service.create(order, buyer.getMainAddress(), List.of()));
    }

    @Test
    void rejectsCreatingTwoShipmentsForTheSameOrder() {
        Order order = order(ProductType.PHYSICAL, true);
        Shipment existing = service.create(order, buyer.getMainAddress(), List.of());

        assertThrows(IllegalStateException.class,
                () -> service.create(order, buyer.getMainAddress(), List.of(existing)));
    }
}