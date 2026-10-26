package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.RefundJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaRefundRepository;
import application.domain.entities.Refund;
import application.domain.ports.out.RefundRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Output persistence adapter for Refund backed by Spring Data JPA (MySQL).
 * The order is rebuilt through JpaReferenceResolver.
 */
@Repository
public class RefundJpaAdapter implements RefundRepositoryPort {

    private final SpringDataJpaRefundRepository repository;
    private final JpaReferenceResolver resolver;

    public RefundJpaAdapter(SpringDataJpaRefundRepository repository,
                            JpaReferenceResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    public Optional<Refund> findById(UUID id) {
        return repository.findById(id.toString())
                .map(entity -> RefundJpaMapper.toDomain(entity, resolver.order(entity.getOrderId())));
    }

    @Override
    public Optional<Refund> findByOrder(UUID orderId) {
        return repository.findByOrderId(orderId.toString())
                .map(entity -> RefundJpaMapper.toDomain(entity, resolver.order(entity.getOrderId())));
    }

    @Override
    public void save(Refund refund) {
        repository.save(RefundJpaMapper.toEntity(refund));
    }
}