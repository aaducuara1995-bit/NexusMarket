package application.adapters.useCases;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.InventoryStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SellerUseCaseTest {

    @Test
    void theRegisteredProductBelongsToTheSignedInSeller() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User seller = ctx.sellerUser(ctx.admin());

        Product product = ctx.sellerUseCase.registerProduct(seller,
                UseCaseTestContext.draftProduct("Mug", ProductType.PHYSICAL));

        assertEquals(ctx.sellerOf(seller).getId(), product.getSellerId());
        assertEquals(1, ctx.sellerUseCase.consultMyProducts(seller).size());
    }

    @Test
    void aPhysicalProductNeedsStockBeforePublishing() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User seller = ctx.sellerUser(ctx.admin());
        Product product = ctx.sellerUseCase.registerProduct(seller,
                UseCaseTestContext.draftProduct("Mug", ProductType.PHYSICAL));

        assertThrows(IllegalStateException.class, () -> ctx.sellerUseCase.publishProduct(seller, product));

        ctx.sellerUseCase.registerInitialInventory(seller, product, ctx.warehouseOf(seller), 4);
        assertEquals(ProductStatus.PUBLISHED, ctx.sellerUseCase.publishProduct(seller, product).getStatus());
        assertEquals(1, ctx.movements.saved.size());
    }

    @Test
    void theSellerManagesTheStockOfTheirProducts() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User seller = ctx.sellerUser(ctx.admin());
        Product product = ctx.physicalProduct(seller, 10);
        Inventory inventory = ctx.inventoryOf(product);

        ctx.sellerUseCase.adjustInventory(seller, inventory, -2, "Count");
        Inventory blocked = ctx.sellerUseCase.blockInventory(seller, inventory);

        assertEquals(8, blocked.getQuantity());
        assertEquals(InventoryStatus.BLOCKED, blocked.getStatus());
        assertEquals(0, ctx.publicAccess.consultAvailability(product));
    }

    @Test
    void aSellerCannotTouchAnotherSellersProduct() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        Product product = ctx.physicalProduct(ctx.sellerUser(admin), 3);
        User otherSeller = ctx.sellerUser(admin);

        assertThrows(AccessDeniedException.class, () -> ctx.sellerUseCase.suspendProduct(otherSeller, product));
        assertThrows(AccessDeniedException.class,
                () -> ctx.sellerUseCase.blockInventory(otherSeller, ctx.inventoryOf(product)));
    }

    @Test
    void onlySellersUseThisPort() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User buyer = ctx.buyerUser();

        assertThrows(AccessDeniedException.class, () -> ctx.sellerUseCase.consultMyProducts(buyer));
        assertThrows(AccessDeniedException.class, () -> ctx.sellerUseCase.registerProduct(
                ctx.registered(ctx.admin(), UserRole.LOGISTIC_OPERATOR),
                UseCaseTestContext.draftProduct("Mug", ProductType.PHYSICAL)));
    }
}