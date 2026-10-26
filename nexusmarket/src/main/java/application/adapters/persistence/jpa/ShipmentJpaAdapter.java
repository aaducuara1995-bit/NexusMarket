package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.ShipmentJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaShipmentRepository;
import application.domain.entities.Shipment;
import application.domain.ports.out.ShipmentRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Output persistence adapter for Shipment backed by Spring Data JPA (MySQL).
 * The order is rebuilt through JpaReferenceResolver.
 */
@Repository
public class ShipmentJpaAdapter implements ShipmentRepositoryPort {

    private final SpringDataJpaShipmentRepository repository;
    private final JpaReferenceResolver resolver;

    public ShipmentJpaAdapter(SpringDataJpaShipmentRepository repository,
                              JpaReferenceResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    public Optional<Shipment> findByOrder(UUID orderId) {
        return repository.findByOrderId(orderId.toString())
                .map(entity -> ShipmentJpaMapper.toDomain(entity, resolver.order(entity.getOrderId())));
    }

    @Override
    public void save(Shipment shipment) {
        repository.save(ShipmentJpaMapper.toEntity(shipment));
    }
}