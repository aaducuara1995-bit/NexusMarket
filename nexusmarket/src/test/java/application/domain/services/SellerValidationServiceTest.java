package application.domain.services;

import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SellerValidationServiceTest {

    private final SellerValidationService service = new SellerValidationService();

    @Test
    void activeSellerCanOperateTheirProduct() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);

        assertDoesNotThrow(() -> service.requireOwner(user, seller, product));
    }

    @Test
    void userWithAnotherRoleIsRejected() {
        User buyer = Fixtures.user(UserRole.BUYER);
        Seller seller = Fixtures.seller(buyer);

        assertThrows(AccessDeniedException.class,
                () -> service.requireSeller(buyer, seller));
    }

    @Test
    void sellerMustMatchTheUser() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller fromAnotherPerson = Fixtures.seller(Fixtures.user(UserRole.SELLER));

        assertThrows(AccessDeniedException.class,
                () -> service.requireSeller(user, fromAnotherPerson));
    }

    @Test
    void anotherSellersProductIsRejected() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product foreign = Fixtures.product(ProductType.PHYSICAL);

        assertThrows(AccessDeniedException.class,
                () -> service.requireOwner(user, seller, foreign));
    }

    @Test
    void blockedUserIsRejected() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        user.block();

        assertThrows(IllegalStateException.class,
                () -> service.requireSeller(user, seller));
    }
}