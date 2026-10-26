package application.adapters.useCases;

import application.domain.entities.Buyer;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PublicAccessUseCaseTest {

    @Test
    void signUpSavesTheUserAndTheBuyerProfile() {
        UseCaseTestContext ctx = new UseCaseTestContext();

        Buyer buyer = ctx.publicAccess.registerBuyer(ctx.newUser(UserRole.BUYER),
                UseCaseTestContext.address("Calle 5"));

        assertEquals(1, ctx.users.saved.size());
        assertTrue(ctx.buyers.findByUserId(buyer.getUserId()).isPresent());
    }

    @Test
    void signUpOnlyCreatesBuyers() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User seller = ctx.newUser(UserRole.SELLER);

        assertThrows(AccessDeniedException.class,
                () -> ctx.publicAccess.registerBuyer(seller, UseCaseTestContext.address("Calle 5")));
        assertTrue(ctx.users.saved.isEmpty());
    }

    @Test
    void theCatalogShowsOnlyPublishedProductsWithTheirStock() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User seller = ctx.sellerUser(ctx.admin());
        Product published = ctx.physicalProduct(seller, 7);
        ctx.sellerUseCase.registerProduct(seller, UseCaseTestContext.draftProduct("Draft", ProductType.PHYSICAL));

        assertEquals(1, ctx.publicAccess.consultCatalog().size());
        assertEquals(7, ctx.publicAccess.consultAvailability(published));
    }
}