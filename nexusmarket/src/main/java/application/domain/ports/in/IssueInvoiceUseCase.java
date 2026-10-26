package application.domain.ports.in;

import application.domain.entities.Invoice;
import application.domain.entities.Order;

import java.util.List;

/** Input port: issues the invoice of a paid order. */
public interface IssueInvoiceUseCase {

    Invoice issue(Order order, List<Invoice> existing);
}