package application.domain.services;

import application.domain.entities.Order;
import application.domain.entities.Refund;
import application.domain.entities.User;
import application.domain.enums.ProductType;
import application.domain.enums.RefundStatus;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcessRefundServiceTest {

    private final ProcessRefundService service =
            new ProcessRefundService(new AuthorizationService());

    private final User admin = Fixtures.user(UserRole.ADMIN);

    private Refund pendingRefund() {
        Order order = Fixtures.deliveredOrder(Fixtures.product(ProductType.DIGITAL), 1);
        return new Refund(order, order.calculateTotal());
    }

    @Test
    void adminProcessesTheRefund() {
        Refund refund = pendingRefund();

        service.process(admin, refund);

        assertEquals(RefundStatus.PROCESSED, refund.getStatus());
    }

    @Test
    void buyerCannotProcessRefunds() {
        Refund refund = pendingRefund();
        User buyer = Fixtures.user(UserRole.BUYER);

        assertThrows(AccessDeniedException.class, () -> service.process(buyer, refund));
        assertEquals(RefundStatus.PENDING, refund.getStatus());
    }

    @Test
    void refundIsNotProcessedTwice() {
        Refund refund = pendingRefund();
        service.process(admin, refund);

        assertThrows(IllegalStateException.class, () -> service.process(admin, refund));
    }

    @Test
    void blockedAdminCannotProcess() {
        Refund refund = pendingRefund();
        admin.block();

        assertThrows(AccessDeniedException.class, () -> service.process(admin, refund));
        assertEquals(RefundStatus.PENDING, refund.getStatus());
    }
}