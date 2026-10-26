package application.adapters.useCases;

import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupervisorUseCaseTest {

    @Test
    void theSupervisorConsultsUsersWarehousesInventoryAndOrders() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        ctx.paidOrder(ctx.buyerUser(), ctx.physicalProduct(ctx.sellerUser(admin), 5), 1);
        User supervisor = ctx.registered(admin, UserRole.SUPERVISOR);

        assertEquals(4, ctx.supervisorUseCase.consultUsers(supervisor).size());
        assertEquals(1, ctx.supervisorUseCase.consultWarehouses(supervisor).size());
        assertEquals(1, ctx.supervisorUseCase.consultInventory(supervisor).size());
        assertEquals(1, ctx.supervisorUseCase.consultOrders(supervisor).size());
    }

    @Test
    void otherRolesCannotReadTheReports() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User buyer = ctx.buyerUser();

        assertThrows(AccessDeniedException.class, () -> ctx.supervisorUseCase.consultOrders(buyer));
    }

    @Test
    void aBlockedSupervisorCannotConsult() {
        UseCaseTestContext ctx = new UseCaseTestContext();
        User admin = ctx.admin();
        User supervisor = ctx.registered(admin, UserRole.SUPERVISOR);
        ctx.adminUseCase.blockUser(admin, supervisor);

        assertThrows(AccessDeniedException.class, () -> ctx.supervisorUseCase.consultUsers(supervisor));
    }
}