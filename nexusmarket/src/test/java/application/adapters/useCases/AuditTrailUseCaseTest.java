package application.adapters.useCases;

import application.domain.entities.AuditLog;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.AuditOperation;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditTrailUseCaseTest {

    @Test
    void theWholeLifeOfAnOrderIsAudited() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        Product product = ctx.physicalProduct(ctx.sellerUser(admin), 10);
        User buyer = ctx.buyerUser();
        Order order = ctx.paidOrder(buyer, product, 2);
        User operator = ctx.registered(admin, UserRole.LOGISTIC_OPERATOR);
        ctx.logisticUseCase.shipOrder(operator, order);
        ctx.logisticUseCase.confirmDelivery(operator, order);
        User supervisor = ctx.registered(admin, UserRole.SUPERVISOR);

        List<AuditLog> ofOrder = ctx.supervisorUseCase.consultAuditLogOf(supervisor, order.getId());

        assertEquals(List.of(AuditOperation.ORDER_DELIVERY, AuditOperation.ORDER_SHIPMENT,
                        AuditOperation.ORDER_PAYMENT, AuditOperation.ORDER_CONFIRMATION),
                ofOrder.stream().map(AuditLog::getOperation).toList());
        assertEquals(buyer.getId(), ofOrder.get(3).getPerformedByUserId());
        assertEquals(UserRole.LOGISTIC_OPERATOR, ofOrder.get(0).getPerformedByRole());
    }

    @Test
    void administrativeOperationsAreAuditedToo() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        User seller = ctx.sellerUser(admin);
        ctx.adminUseCase.blockUser(admin, seller);

        List<AuditOperation> operations = ctx.adminUseCase.consultAuditLog(admin).stream()
                .map(AuditLog::getOperation).toList();

        assertTrue(operations.contains(AuditOperation.USER_REGISTRATION));
        assertTrue(operations.contains(AuditOperation.SELLER_ONBOARDING));
        assertTrue(operations.contains(AuditOperation.USER_BLOCKING));
    }

    @Test
    void aFailedOperationLeavesNoTrace() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User seller = ctx.sellerUser(ctx.admin());
        Product product = ctx.physicalProduct(seller, 3);
        int before = ctx.auditLogs.saved.size();

        assertThrows(IllegalStateException.class,
                () -> ctx.sellerUseCase.adjustInventory(seller, ctx.inventoryOf(product), -5, "Count"));

        assertEquals(before, ctx.auditLogs.saved.size());
    }

    @Test
    void onlySupervisorAndAdminReadTheTrail() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User buyer = ctx.buyerUser();

        assertThrows(AccessDeniedException.class, () -> ctx.supervisorUseCase.consultAuditLog(buyer));
    }
}