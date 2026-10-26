package application.adapters.useCases;

import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.MovementType;
import application.domain.enums.OrderStatus;
import application.domain.enums.ReservationStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogisticOperatorUseCaseTest {

    @Test
    void theOperatorShipsAndConfirmsDelivery() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        Product product = ctx.physicalProduct(ctx.sellerUser(admin), 10);
        Order order = ctx.paidOrder(ctx.buyerUser(), product, 4);
        User operator = ctx.registered(admin, UserRole.LOGISTIC_OPERATOR);

        assertEquals(1, ctx.logisticUseCase.consultOrdersToShip(operator).size());

        ctx.logisticUseCase.shipOrder(operator, order);
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertEquals(6, ctx.inventoryOf(product).getQuantity());
        assertTrue(ctx.reservations.findByOrder(order.getId()).stream()
                .allMatch(r -> r.getStatus() == ReservationStatus.CONFIRMED));
        assertTrue(ctx.movements.saved.stream().anyMatch(m -> m.getType() == MovementType.SALE));

        ctx.logisticUseCase.confirmDelivery(operator, order);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(ShipmentStatus.DELIVERED, ctx.shipments.findByOrder(order.getId()).orElseThrow().getStatus());
        assertTrue(ctx.logisticUseCase.consultOrdersToShip(operator).isEmpty());
    }

    @Test
    void digitalOrdersNeverReachTheWarehouse() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        ctx.paidOrder(ctx.buyerUser(), ctx.digitalProduct(ctx.sellerUser(admin)), 1);
        User operator = ctx.registered(admin, UserRole.LOGISTIC_OPERATOR);

        assertTrue(ctx.logisticUseCase.consultOrdersToShip(operator).isEmpty());
    }

    @Test
    void theOperatorAdjustsStockOfAnySeller() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        Product product = ctx.physicalProduct(ctx.sellerUser(admin), 10);
        User operator = ctx.registered(admin, UserRole.LOGISTIC_OPERATOR);

        ctx.logisticUseCase.adjustInventory(operator, ctx.inventoryOf(product), 5, "Supplier delivery");

        assertEquals(15, ctx.inventoryOf(product).getQuantity());
    }

    @Test
    void onlyOperatorsUseThisPort() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        User seller = ctx.sellerUser(admin);
        Product product = ctx.physicalProduct(seller, 10);

        assertThrows(AccessDeniedException.class,
                () -> ctx.logisticUseCase.adjustInventory(seller, ctx.inventoryOf(product), 1, "Count"));
        assertThrows(AccessDeniedException.class, () -> ctx.logisticUseCase.consultOrdersToShip(ctx.buyerUser()));
    }
}