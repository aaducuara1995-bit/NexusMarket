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

class InventoryAccessServiceTest {

    private final InventoryAccessService service = new InventoryAccessService(
            new AuthorizationService(), new SellerValidationService());

    @Test
    void owningSellerHasAccess() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product product = Fixtures.product(seller.getId(), ProductType.PHYSICAL);

        assertDoesNotThrow(() -> service.requireAccess(user, seller, product));
    }

    @Test
    void logisticOperatorHasAccessWithoutBeingOwner() {
        User operator = Fixtures.user(UserRole.LOGISTIC_OPERATOR);
        Product product = Fixtures.product(ProductType.PHYSICAL);

        assertDoesNotThrow(() -> service.requireAccess(operator, null, product));
    }

    @Test
    void sellerCannotAccessInventoryOfOtherSellersProducts() {
        User user = Fixtures.user(UserRole.SELLER);
        Seller seller = Fixtures.seller(user);
        Product foreign = Fixtures.product(ProductType.PHYSICAL);

        assertThrows(AccessDeniedException.class,
                () -> service.requireAccess(user, seller, foreign));
    }

    @Test
    void buyerHasNoAccess() {
        User buyer = Fixtures.user(UserRole.BUYER);
        Product product = Fixtures.product(ProductType.PHYSICAL);

        assertThrows(AccessDeniedException.class,
                () -> service.requireAccess(buyer, null, product));
    }

    @Test
    void sellerWithoutProfileIsRejected() {
        User user = Fixtures.user(UserRole.SELLER);
        Product product = Fixtures.product(ProductType.PHYSICAL);

        assertThrows(IllegalArgumentException.class,
                () -> service.requireAccess(user, null, product));
    }
}