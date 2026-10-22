package application.domain.services;

import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegisterProductServiceTest {

    private final InMemoryRepositories.Products repo = new InMemoryRepositories.Products();
    private final RegisterProductService service = new RegisterProductService(
            new AuthorizationService(), new SellerValidationService(), repo);

    private final User user = Fixtures.user(UserRole.SELLER);
    private final Seller seller = Fixtures.seller(user);
    private final Money price = new Money(new BigDecimal("45000"));

    @Test
    void sellerRegistersAProductThatStartsSuspended() {
        Product product = service.register(user, seller, "T-shirt", "Cotton",
                ProductType.PHYSICAL, price);

        assertEquals(ProductStatus.SUSPENDED, product.getStatus());
        assertEquals(seller.getId(), product.getSellerId());
        assertEquals(1, repo.saved.size());
    }

    @Test
    void sellerDoesNotRepeatProductNames() {
        service.register(user, seller, "T-shirt", "Cotton", ProductType.PHYSICAL, price);

        assertThrows(IllegalStateException.class,
                () -> service.register(user, seller, "t-shirt", "Other", ProductType.PHYSICAL, price));
        assertEquals(1, repo.saved.size());
    }

    @Test
    void twoSellersCanUseTheSameName() {
        User otherUser = Fixtures.user(UserRole.SELLER);
        Seller other = Fixtures.seller(otherUser);
        service.register(user, seller, "T-shirt", "Cotton", ProductType.PHYSICAL, price);

        service.register(otherUser, other, "T-shirt", "Linen", ProductType.PHYSICAL, price);

        assertEquals(2, repo.saved.size());
    }

    @Test
    void buyerDoesNotRegisterProducts() {
        User buyer = Fixtures.user(UserRole.BUYER);

        assertThrows(AccessDeniedException.class,
                () -> service.register(buyer, Fixtures.seller(buyer), "T-shirt",
                        "Cotton", ProductType.PHYSICAL, price));
    }

    @Test
    void rejectsAZeroPriceWithoutSavingAnything() {
        Money zero = new Money(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class,
                () -> service.register(user, seller, "T-shirt", "Cotton", ProductType.PHYSICAL, zero));
        assertEquals(0, repo.saved.size());
    }
}