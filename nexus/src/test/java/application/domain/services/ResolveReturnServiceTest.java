package application.domain.services;

import application.domain.entities.ReturnRequest;
import application.domain.entities.User;
import application.domain.enums.ProductType;
import application.domain.enums.ReturnStatus;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResolveReturnServiceTest {

    private final ResolveReturnService service =
            new ResolveReturnService(new AuthorizationService());

    private final User admin = Fixtures.user(UserRole.ADMIN);

    private ReturnRequest requestedReturn() {
        return new ReturnRequest(Fixtures.deliveredOrder(Fixtures.product(ProductType.DIGITAL), 1),
                "Arrived damaged");
    }

    @Test
    void adminApprovesTheReturn() {
        ReturnRequest returnRequest = requestedReturn();

        service.approve(admin, returnRequest);

        assertEquals(ReturnStatus.APPROVED, returnRequest.getStatus());
    }

    @Test
    void adminRejectsTheReturn() {
        ReturnRequest returnRequest = requestedReturn();

        service.reject(admin, returnRequest);

        assertEquals(ReturnStatus.REJECTED, returnRequest.getStatus());
    }

    @Test
    void buyerCannotResolveTheirOwnReturn() {
        ReturnRequest returnRequest = requestedReturn();
        User buyer = Fixtures.user(UserRole.BUYER);

        assertThrows(AccessDeniedException.class, () -> service.approve(buyer, returnRequest));
        assertEquals(ReturnStatus.REQUESTED, returnRequest.getStatus());
    }

    @Test
    void logisticOperatorCannotResolveReturns() {
        ReturnRequest returnRequest = requestedReturn();
        User operator = Fixtures.user(UserRole.LOGISTIC_OPERATOR);

        assertThrows(AccessDeniedException.class, () -> service.reject(operator, returnRequest));
    }

    @Test
    void returnIsResolvedOnlyOnce() {
        ReturnRequest returnRequest = requestedReturn();
        service.approve(admin, returnRequest);

        assertThrows(IllegalStateException.class, () -> service.reject(admin, returnRequest));
        assertEquals(ReturnStatus.APPROVED, returnRequest.getStatus());
    }
}