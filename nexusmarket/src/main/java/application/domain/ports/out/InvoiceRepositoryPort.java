package application.domain.ports.out;

import application.domain.entities.Invoice;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepositoryPort {

    Optional<Invoice> findByOrder(UUID orderId);

    void save(Invoice invoice);
}