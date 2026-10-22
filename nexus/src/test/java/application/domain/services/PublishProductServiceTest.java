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

class PublishProductServiceTest {

    private final PublishProductService service = new PublishProductService(
            new AuthorizationService(), new SellerValidationService());

    @Test
    void publishesAPhysicalProductWithStock() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory inventory = Fixtures.inventory(product, 5);

        service.publish(user, seller, product, List.of(inventory));

        assertEquals(ProductStatus.PUBLISHED, product.getStatus());
    }

    @Test
    void rejectsAPhysicalProductWithoutStock() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);

        assertThrows(IllegalStateException.class,
                () -> service.publish(user, seller, product, List.of()));
        assertEquals(ProductStatus.SUSPENDED, product.getStatus());
    }

    @Test
    void doesNotCountDamagedInventory() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);
        Inventory damaged = Fixtures.inventory(product, 5);
        damaged.markDamaged();

        assertThrows(IllegalStateException.class,
                () -> service.publish(user, seller, product, List.of(damaged)));
    }

    @Test
    void publishesADigitalProductWithoutInventory() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.DIGITAL);

        service.publish(user, seller, product, List.of());

        assertEquals(ProductStatus.PUBLISHED, product.getStatus());
    }

    @Test
    void sellerCannotPublishAnotherSellersProduct() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product foreign = Fixtures.product(ProductType.DIGITAL);

        assertThrows(AccessDeniedException.class,
                () -> service.publish(user, seller, foreign, List.of()));
    }

    @Test
    void buyerCannotPublishProducts() {
        User buyer = Fixtures.user(UserRole.BUYER);
        Seller seller = Fixtures.seller(buyer);
        Product product = Fixtures.product(seller.getId(), ProductType.DIGITAL);

        assertThrows(AccessDeniedException.class,
                () -> service.publish(buyer, seller, product, List.of()));
    }

    @Test
    void discontinuedProductIsNotPublishedAgain() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.DIGITAL);
        product.discontinue();

        assertThrows(IllegalStateException.class,
                () -> service.publish(user, seller, product, List.of()));
    }
}