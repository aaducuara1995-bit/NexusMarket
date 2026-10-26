package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data JPA repository for InvoiceJpaEntity. Query methods are derived from their names. */
public interface SpringDataJpaInvoiceRepository extends JpaRepository<InvoiceJpaEntity, String> {

    Optional<InvoiceJpaEntity> findByOrderId(String orderId);
}