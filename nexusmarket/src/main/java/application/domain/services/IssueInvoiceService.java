package application.domain.services;

import application.domain.entities.Invoice;
import application.domain.entities.Order;

import java.util.List;

public class IssueInvoiceService {

    public Invoice issue(Order order, List<Invoice> existing) {
        if (order == null)
            throw new IllegalArgumentException("Order is required.");
        if (existing == null)
            throw new IllegalArgumentException("Existing invoice list is required.");

        boolean alreadyInvoiced = existing.stream()
                .anyMatch(f -> f.getOrder().getId().equals(order.getId()));
        if (alreadyInvoiced)
            throw new IllegalStateException("The order has already been invoiced.");

        // The invoice itself requires a paid order and takes the order total.
        return new Invoice(order);
    }
}