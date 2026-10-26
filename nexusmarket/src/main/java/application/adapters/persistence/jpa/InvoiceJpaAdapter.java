package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.InvoiceJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInvoiceRepository;
import application.domain.entities.Invoice;
import application.domain.ports.out.InvoiceRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Output persistence adapter for Invoice backed by Spring Data JPA (MySQL).
 * The order is rebuilt through JpaReferenceResolver.
 */
@Repository
public class InvoiceJpaAdapter implements InvoiceRepositoryPort {

    private final SpringDataJpaInvoiceRepository repository;
    private final JpaReferenceResolver resolver;

    public InvoiceJpaAdapter(SpringDataJpaInvoiceRepository repository,
                             JpaReferenceResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    public Optional<Invoice> findByOrder(UUID orderId) {
        return repository.findByOrderId(orderId.toString())
                .map(entity -> InvoiceJpaMapper.toDomain(entity, resolver.order(entity.getOrderId())));
    }

    @Override
    public void save(Invoice invoice) {
        repository.save(InvoiceJpaMapper.toEntity(invoice));
    }
}