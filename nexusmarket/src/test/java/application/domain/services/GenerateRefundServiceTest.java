package application.domain.services;

import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;
import application.domain.enums.ProductType;
import application.domain.enums.RefundStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenerateRefundServiceTest {

    private final GenerateRefundService service = new GenerateRefundService();

    private ReturnRequest returnRequest() {
        return new ReturnRequest(Fixtures.deliveredOrder(Fixtures.product(ProductType.DIGITAL), 2),
                "Arrived damaged");
    }

    @Test
    void generatesTheRefundForTheOrderTotal() {
        ReturnRequest returnRequest = returnRequest();
        returnRequest.approve();

        Refund refund = service.generate(returnRequest, List.of());

        assertEquals(RefundStatus.PENDING, refund.getStatus());
        assertEquals(0, refund.getAmount().amount()
                .compareTo(returnRequest.getOrder().calculateTotal().amount()));
    }

    @Test
    void rejectsAReturnStillRequested() {
        ReturnRequest returnRequest = returnRequest();

        assertThrows(IllegalStateException.class, () -> service.generate(returnRequest, List.of()));
    }

    @Test
    void rejectsARejectedReturn() {
        ReturnRequest returnRequest = returnRequest();
        returnRequest.reject();

        assertThrows(IllegalStateException.class, () -> service.generate(returnRequest, List.of()));
    }

    @Test
    void doesNotGenerateTwoRefundsForTheSameOrder() {
        ReturnRequest returnRequest = returnRequest();
        returnRequest.approve();
        Refund existing = service.generate(returnRequest, List.of());

        assertThrows(IllegalStateException.class,
                () -> service.generate(returnRequest, List.of(existing)));
    }
}