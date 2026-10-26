package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChangeProductStatusServiceTest {

    private final ChangeProductStatusService service = new ChangeProductStatusService(
            new AuthorizationService(), new SellerValidationService());

    @Test
    void sellerSuspendsTheirPublishedProduct() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.DIGITAL);
        product.publish();

        service.suspend(user, seller, product);

        assertEquals(ProductStatus.SUSPENDED, product.getStatus());
    }

    @Test
    void sellerDiscontinuesTheirProduct() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.DIGITAL);

        service.discontinue(user, seller, product, List.of());

        assertEquals(ProductStatus.DISCONTINUED, product.getStatus());
    }

    @Test
    void productWithActiveReservationsIsNotDiscontinued() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 10);
        inventory.reserve(2);

        assertThrows(IllegalStateException.class,
                () -> service.discontinue(user, seller, product, List.of(inventory)));
        assertEquals(ProductStatus.SUSPENDED, product.getStatus());
    }

    @Test
    void discontinuedProductCannotBeSuspended() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.DIGITAL);
        service.discontinue(user, seller, product, List.of());

        assertThrows(IllegalStateException.class,
                () -> service.suspend(user, seller, product));
    }

    @Test
    void sellerCannotChangeAnotherSellersProduct() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product foreign = Fixtures.product(ProductType.DIGITAL);
        foreign.publish();

        assertThrows(AccessDeniedException.class,
                () -> service.suspend(user, seller, foreign));
    }

    @Test
    void buyerCannotSuspendProducts() {
        User buyer = Fixtures.user(UserRole.BUYER);
        Seller seller = Fixtures.seller(buyer);
        Product product = Fixtures.product(seller.getId(), ProductType.DIGITAL);
        product.publish();

        assertThrows(AccessDeniedException.class,
                () -> service.suspend(buyer, seller, product));
    }
}