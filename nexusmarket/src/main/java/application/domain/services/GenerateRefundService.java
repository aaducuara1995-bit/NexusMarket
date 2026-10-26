package application.domain.services;

import application.domain.entities.Order;
import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;

import java.util.List;

public class GenerateRefundService {

    public Refund generate(ReturnRequest returnRequest, List<Refund> existing) {
        if (returnRequest == null)
            throw new IllegalArgumentException("Return is required.");
        if (existing == null)
            throw new IllegalArgumentException("Existing refund list is required.");

        if (!returnRequest.isApproved())
            throw new IllegalStateException("A refund can only be generated for an approved return.");

        Order order = returnRequest.getOrder();
        boolean alreadyRefunded = existing.stream()
                .anyMatch(r -> r.getOrder().getId().equals(order.getId()));
        if (alreadyRefunded)
            throw new IllegalStateException("The order already has a refund.");

        // The return covers the whole order, so its total is refunded.
        return new Refund(order, order.calculateTotal());
    }
}