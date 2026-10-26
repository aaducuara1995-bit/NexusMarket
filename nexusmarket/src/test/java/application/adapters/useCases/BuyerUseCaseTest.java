package application.adapters.useCases;

import application.domain.entities.Cart;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.OrderStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.valueobjects.Address;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuyerUseCaseTest {

    @Test
    void payingAPhysicalOrderIssuesTheInvoiceAndCreatesTheShipment() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        Product product = ctx.physicalProduct(ctx.sellerUser(ctx.admin()), 10);
        User buyer = ctx.buyerUser();

        Order order = ctx.paidOrder(buyer, product, 3);

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertTrue(ctx.invoices.findByOrder(order.getId()).isPresent());
        assertEquals(ShipmentStatus.PENDING, ctx.shipments.findByOrder(order.getId()).orElseThrow().getStatus());
        assertEquals(7, ctx.publicAccess.consultAvailability(product));
        assertTrue(ctx.buyerUseCase.consultMyCart(buyer).getItems().isEmpty());
    }

    @Test
    void aDigitalOrderIsDeliveredAsSoonAsItIsPaid() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        Product ebook = ctx.digitalProduct(ctx.sellerUser(ctx.admin()));

        Order order = ctx.paidOrder(ctx.buyerUser(), ebook, 1);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertTrue(ctx.invoices.findByOrder(order.getId()).isPresent());
        assertTrue(ctx.shipments.findByOrder(order.getId()).isEmpty());
    }

    @Test
    void theOrderUsesTheAddressTheBuyerChooses() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        Product product = ctx.physicalProduct(ctx.sellerUser(ctx.admin()), 5);
        User user = ctx.buyerUser();
        Address office = UseCaseTestContext.address("Oficina 404");
        ctx.buyers.findByUserId(user.getId()).orElseThrow().addAdditionalAddress(office);

        ctx.buyerUseCase.addToCart(user, product, 1);
        Order order = ctx.buyerUseCase.confirmOrder(user, office);
        ctx.buyerUseCase.payOrder(user, order, order.calculateTotal());

        assertEquals(office, ctx.shipments.findByOrder(order.getId()).orElseThrow().getDeliveryAddress());
    }

    @Test
    void aForeignAddressIsRejectedAndTheCartStaysAsItWas() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        Product product = ctx.physicalProduct(ctx.sellerUser(ctx.admin()), 5);
        User user = ctx.buyerUser();
        ctx.buyerUseCase.addToCart(user, product, 2);

        assertThrows(IllegalArgumentException.class,
                () -> ctx.buyerUseCase.confirmOrder(user, UseCaseTestContext.address("Otra casa")));

        assertEquals(1, ctx.buyerUseCase.consultMyCart(user).getItems().size());
        assertTrue(ctx.orders.saved.isEmpty());
        assertEquals(5, ctx.publicAccess.consultAvailability(product));
    }

    @Test
    void productsCanBeRemovedFromTheCart() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        Product product = ctx.physicalProduct(ctx.sellerUser(ctx.admin()), 5);
        User user = ctx.buyerUser();
        ctx.buyerUseCase.addToCart(user, product, 2);

        Cart cart = ctx.buyerUseCase.removeFromCart(user, product);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void aBlockedBuyerCannotBuy() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        Product product = ctx.physicalProduct(ctx.sellerUser(admin), 5);
        User user = ctx.buyerUser();
        ctx.adminUseCase.blockUser(admin, user);

        assertThrows(IllegalStateException.class, () -> ctx.buyerUseCase.addToCart(user, product, 1));
    }

    @Test
    void onlyBuyersUseThisPort() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User operator = ctx.registered(ctx.admin(), UserRole.LOGISTIC_OPERATOR);

        assertThrows(AccessDeniedException.class, () -> ctx.buyerUseCase.consultMyOrders(operator));
    }

    @Test
    void theBuyerSeesOnlyTheirOwnOrders() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        Product product = ctx.physicalProduct(ctx.sellerUser(ctx.admin()), 10);
        User ana = ctx.buyerUser();
        User luis = ctx.buyerUser();
        ctx.paidOrder(ana, product, 1);
        ctx.paidOrder(luis, product, 1);

        assertEquals(1, ctx.buyerUseCase.consultMyOrders(ana).size());
    }
}