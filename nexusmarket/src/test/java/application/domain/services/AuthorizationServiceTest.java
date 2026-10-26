package application.domain.services;

import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationServiceTest {

    private final AuthorizationService service = new AuthorizationService();

    private User user(UserRole role) {
        return new User(
                new FullName("Ana", "Perez"),
                new Email("ana@mail.com"),
                new IdentityDocument("123456"),
                role);
    }

    @Test
    void onlyAdminRegistersSellers() {
        assertTrue(service.canExecute(user(UserRole.ADMIN), BusinessProcess.SELLER_REGISTRATION));
        assertFalse(service.canExecute(user(UserRole.SELLER), BusinessProcess.SELLER_REGISTRATION));
        assertFalse(service.canExecute(user(UserRole.BUYER), BusinessProcess.SELLER_REGISTRATION));
    }

    @Test
    void onlySellerRegistersProducts() {
        assertTrue(service.canExecute(user(UserRole.SELLER), BusinessProcess.PRODUCT_REGISTRATION));
        assertFalse(service.canExecute(user(UserRole.ADMIN), BusinessProcess.PRODUCT_REGISTRATION));
        assertFalse(service.canExecute(user(UserRole.LOGISTIC_OPERATOR), BusinessProcess.PRODUCT_REGISTRATION));
    }

    @Test
    void inventoryIsManagedBySellerAndLogisticOperator() {
        assertTrue(service.canExecute(user(UserRole.SELLER), BusinessProcess.INVENTORY_MANAGEMENT));
        assertTrue(service.canExecute(user(UserRole.LOGISTIC_OPERATOR), BusinessProcess.INVENTORY_MANAGEMENT));
        assertFalse(service.canExecute(user(UserRole.BUYER), BusinessProcess.INVENTORY_MANAGEMENT));
    }

    @Test
    void refundsAreManagedByBuyerAndAdmin() {
        assertTrue(service.canExecute(user(UserRole.BUYER), BusinessProcess.REFUND_MANAGEMENT));
        assertTrue(service.canExecute(user(UserRole.ADMIN), BusinessProcess.REFUND_MANAGEMENT));
        assertFalse(service.canExecute(user(UserRole.SELLER), BusinessProcess.REFUND_MANAGEMENT));
    }

    @Test
    void supervisorOnlyConsultsReports() {
        User supervisor = user(UserRole.SUPERVISOR);
        for (BusinessProcess process : BusinessProcess.values()) {
            if (process == BusinessProcess.ADMINISTRATIVE_REPORTS)
                assertTrue(service.canExecute(supervisor, process));
            else
                assertFalse(service.canExecute(supervisor, process));
        }
    }

    @Test
    void onlyTheAdminManagesUsers() {
        assertTrue(service.canExecute(user(UserRole.ADMIN), BusinessProcess.USER_MANAGEMENT));
        assertFalse(service.canExecute(user(UserRole.SUPERVISOR), BusinessProcess.USER_MANAGEMENT));
        assertFalse(service.canExecute(user(UserRole.BUYER), BusinessProcess.USER_MANAGEMENT));
    }

    @Test
    void requirePermissionThrowsIfRoleDoesNotMatch() {
        assertThrows(AccessDeniedException.class,
                () -> service.requirePermission(user(UserRole.BUYER), BusinessProcess.PRODUCT_REGISTRATION));
    }

    @Test
    void requirePermissionDoesNotThrowIfRoleMatches() {
        assertDoesNotThrow(
                () -> service.requirePermission(user(UserRole.SELLER), BusinessProcess.PRODUCT_REGISTRATION));
    }

    @Test
    void blockedUserCannotExecuteAnything() {
        User blocked = user(UserRole.BUYER);
        blocked.block();

        assertFalse(service.canExecute(blocked, BusinessProcess.ORDER_MANAGEMENT));
        assertThrows(AccessDeniedException.class,
                () -> service.requirePermission(blocked, BusinessProcess.ORDER_MANAGEMENT));
    }
}