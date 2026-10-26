package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.enums.ProductType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckAvailabilityServiceTest {

    private final CheckAvailabilityService service = new CheckAvailabilityService();

    @Test
    void sumsAvailableStockAcrossWarehousesExcludingReserved() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Inventory a = Fixtures.inventory(product, 10);
        Inventory b = Fixtures.inventory(product, 5);
        a.reserve(3);

        assertEquals(12, service.availableOf(product, List.of(a, b)));
    }

    @Test
    void ignoresDamagedInventory() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Inventory good = Fixtures.inventory(product, 10);
        Inventory damaged = Fixtures.inventory(product, 5);
        damaged.markDamaged();

        assertEquals(10, service.availableOf(product, List.of(good, damaged)));
    }

    @Test
    void ignoresInventoryOfOtherProducts() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Product other = Fixtures.product(ProductType.PHYSICAL);

        assertEquals(0, service.availableOf(product, List.of(Fixtures.inventory(other, 8))));
    }

    @Test
    void digitalProductIsAlwaysAvailable() {
        Product digital = Fixtures.product(ProductType.DIGITAL);

        assertTrue(service.hasAvailability(digital, 5, List.of()));
    }

    @Test
    void physicalProductWithoutEnoughStockIsNotAvailable() {
        Product product = Fixtures.product(ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 4);

        assertTrue(service.hasAvailability(product, 4, List.of(inventory)));
        assertFalse(service.hasAvailability(product, 5, List.of(inventory)));
    }
}