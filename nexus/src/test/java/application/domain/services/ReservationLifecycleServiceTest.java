package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.enums.MovementType;
import application.domain.enums.ProductType;
import application.domain.enums.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationLifecycleServiceTest {

    private final ReserveInventoryService reservationService = new ReserveInventoryService();
    private final ReleaseReservationService releaseService = new ReleaseReservationService();
    private final InventoryOutflowService outflowService = new InventoryOutflowService();

    @Test
    void releasingReturnsUnitsToAvailable() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        var reservations = reservationService.reserve(UUID.randomUUID(), product, 4, List.of(inventory)).reservations();

        var movements = releaseService.release(reservations);

        assertEquals(0, inventory.getReserved());
        assertEquals(10, inventory.getAvailable());
        assertEquals(ReservationStatus.RELEASED, reservations.get(0).getStatus());
        assertEquals(-4, movements.get(0).getQuantity());
        assertEquals(MovementType.RESERVATION, movements.get(0).getType());
    }

    @Test
    void reservationCannotBeReleasedTwice() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        var reservations = reservationService.reserve(UUID.randomUUID(), product, 4, List.of(inventory)).reservations();
        releaseService.release(reservations);

        assertThrows(IllegalStateException.class, () -> releaseService.release(reservations));
    }

    @Test
    void confirmingOutflowDeductsStockPermanently() {
        UUID orderId = UUID.randomUUID();
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        var reservations = reservationService.reserve(orderId, product, 4, List.of(inventory)).reservations();

        var movements = outflowService.confirmOutflow(orderId, reservations);

        assertEquals(6, inventory.getQuantity());
        assertEquals(0, inventory.getReserved());
        assertEquals(ReservationStatus.CONFIRMED, reservations.get(0).getStatus());
        assertEquals(MovementType.SALE, movements.get(0).getType());
        assertEquals(-4, movements.get(0).getQuantity());
    }

    @Test
    void outflowRejectsReservationsOfAnotherOrder() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        var reservations = reservationService.reserve(UUID.randomUUID(), product, 4, List.of(inventory)).reservations();

        assertThrows(IllegalArgumentException.class,
                () -> outflowService.confirmOutflow(UUID.randomUUID(), reservations));
        assertEquals(4, inventory.getReserved());
    }
}