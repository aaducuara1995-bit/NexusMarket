package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.ReturnRequestJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaReturnRequestRepository;
import application.domain.entities.ReturnRequest;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output persistence adapter for ReturnRequest backed by Spring Data JPA (MySQL).
 * The order is rebuilt through JpaReferenceResolver.
 */
@Repository
public class ReturnRequestJpaAdapter implements ReturnRequestRepositoryPort {

    private final SpringDataJpaReturnRequestRepository repository;
    private final JpaReferenceResolver resolver;

    public ReturnRequestJpaAdapter(SpringDataJpaReturnRequestRepository repository,
                                   JpaReferenceResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    public Optional<ReturnRequest> findById(UUID id) {
        return repository.findById(id.toString())
                .map(entity -> ReturnRequestJpaMapper.toDomain(entity, resolver.order(entity.getOrderId())));
    }

    @Override
    public List<ReturnRequest> findByOrder(UUID orderId) {
        return repository.findByOrderId(orderId.toString()).stream()
                .map(entity -> ReturnRequestJpaMapper.toDomain(entity, resolver.order(entity.getOrderId())))
                .toList();
    }

    @Override
    public void save(ReturnRequest returnRequest) {
        repository.save(ReturnRequestJpaMapper.toEntity(returnRequest));
    }
}