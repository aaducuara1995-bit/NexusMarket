package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.enums.MovementType;
import application.domain.enums.ProductType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReserveInventoryServiceTest {

    private final ReserveInventoryService service = new ReserveInventoryService();

    @Test
    void reservesFromASingleWarehouse() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);

        var result = service.reserve(UUID.randomUUID(), product, 4, List.of(inventory));

        assertEquals(1, result.reservations().size());
        assertEquals(4, inventory.getReserved());
        assertEquals(MovementType.RESERVATION, result.movements().get(0).getType());
        assertEquals(4, result.movements().get(0).getQuantity());
    }

    @Test
    void splitsTheOrderAcrossWarehousesStartingWithTheLargest() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Inventory a = Fixtures.inventory(product, 3);
        Inventory b = Fixtures.inventory(product, 5);

        var result = service.reserve(UUID.randomUUID(), product, 6, List.of(a, b));

        assertEquals(2, result.reservations().size());
        assertEquals(5, b.getReserved());
        assertEquals(1, a.getReserved());
    }

    @Test
    void reservesNothingIfStockIsInsufficient() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Inventory a = Fixtures.inventory(product, 3);
        Inventory b = Fixtures.inventory(product, 2);

        assertThrows(IllegalStateException.class,
                () -> service.reserve(UUID.randomUUID(), product, 6, List.of(a, b)));

        assertEquals(0, a.getReserved());
        assertEquals(0, b.getReserved());
    }

    @Test
    void doesNotUseDamagedInventory() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Inventory damaged = Fixtures.inventory(product, 10);
        damaged.markDamaged();
        Inventory good = Fixtures.inventory(product, 4);

        assertThrows(IllegalStateException.class,
                () -> service.reserve(UUID.randomUUID(), product, 5, List.of(damaged, good)));
    }

    @Test
    void digitalProductReservesNothing() {
        Product digital = Fixtures.product(ProductType.DIGITAL);

        var result = service.reserve(UUID.randomUUID(), digital, 3, List.of());

        assertTrue(result.reservations().isEmpty());
        assertTrue(result.movements().isEmpty());
    }

    @Test
    void rejectsInvalidQuantities() {
        Product product = Fixtures.product(ProductType.PHYSICAL);

        assertThrows(IllegalArgumentException.class,
                () -> service.reserve(UUID.randomUUID(), product, 0, List.of()));
    }
}