package application.adapters.useCases;

import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.enums.RefundStatus;
import application.domain.enums.ReturnStatus;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminUseCaseTest {

    @Test
    void onboardingCreatesTheSellerAndTheirFirstWarehouse() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        User sellerUser = ctx.registered(admin, UserRole.SELLER);

        Seller seller = ctx.adminUseCase.onboardSeller(admin, sellerUser,
                UseCaseTestContext.draftWarehouse("Norte"));

        assertTrue(ctx.sellers.findById(seller.getId()).isPresent());
        assertTrue(ctx.warehouses.findAll().get(0).belongsTo(seller.getId()));
        assertThrows(IllegalStateException.class, () -> ctx.adminUseCase.onboardSeller(admin, sellerUser,
                UseCaseTestContext.draftWarehouse("Sur")));
    }

    @Test
    void theAdminBlocksAndReactivatesUsersButNotThemself() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        User buyer = ctx.buyerUser();

        assertEquals(UserStatus.BLOCKED, ctx.adminUseCase.blockUser(admin, buyer).getStatus());
        assertEquals(UserStatus.ACTIVE, ctx.adminUseCase.activateUser(admin, buyer).getStatus());
        assertThrows(IllegalStateException.class, () -> ctx.adminUseCase.blockUser(admin, admin));
    }

    @Test
    void approvingAReturnGeneratesTheRefundAndTheUnitsGoBackToStock() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        User seller = ctx.sellerUser(admin);
        Product product = ctx.physicalProduct(seller, 10);
        User buyer = ctx.buyerUser();
        Order order = ctx.paidOrder(buyer, product, 2);
        User operator = ctx.registered(admin, UserRole.LOGISTIC_OPERATOR);
        ctx.logisticUseCase.shipOrder(operator, order);
        ctx.logisticUseCase.confirmDelivery(operator, order);

        ReturnRequest returnRequest = ctx.buyerUseCase.requestReturn(buyer, order, "Wrong size");
        ctx.adminUseCase.approveReturn(admin, returnRequest);

        assertEquals(ReturnStatus.APPROVED, returnRequest.getStatus());
        Refund refund = ctx.refunds.findByOrder(order.getId()).orElseThrow();
        assertEquals(order.calculateTotal(), refund.getAmount());
        assertEquals(RefundStatus.PROCESSED, ctx.adminUseCase.processRefund(admin, refund).getStatus());

        ctx.sellerUseCase.restockFromReturn(seller, ctx.inventoryOf(product), returnRequest, 2);
        assertEquals(10, ctx.inventoryOf(product).getQuantity());
    }

    @Test
    void aRejectedReturnHasNoRefund() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        User buyer = ctx.buyerUser();
        Order order = ctx.paidOrder(buyer, ctx.digitalProduct(ctx.sellerUser(admin)), 1);

        ReturnRequest returnRequest = ctx.buyerUseCase.requestReturn(buyer, order, "Not what I expected");
        ctx.adminUseCase.rejectReturn(admin, returnRequest);

        assertEquals(ReturnStatus.REJECTED, returnRequest.getStatus());
        assertTrue(ctx.refunds.saved.isEmpty());
    }

    @Test
    void onlyTheAdminUsesThisPort() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User supervisor = ctx.registered(ctx.admin(), UserRole.SUPERVISOR);

        assertThrows(AccessDeniedException.class,
                () -> ctx.adminUseCase.registerUser(supervisor, ctx.newUser(UserRole.BUYER), "secret123"));
        assertThrows(AccessDeniedException.class,
                () -> ctx.adminUseCase.registerMarketplaceWarehouse(supervisor,
                        UseCaseTestContext.draftWarehouse("Centro")));
    }
}