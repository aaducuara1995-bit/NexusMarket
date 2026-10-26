package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.InventoryReservationJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInventoryReservationRepository;
import application.domain.entities.Inventory;
import application.domain.entities.InventoryReservation;
import application.domain.ports.out.InventoryReservationRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Output persistence adapter for InventoryReservation backed by Spring Data JPA (MySQL).
 * Reservations of the same inventory share one rebuilt Inventory, so their changes add up.
 */
@Repository
public class InventoryReservationJpaAdapter implements InventoryReservationRepositoryPort {

    private final SpringDataJpaInventoryReservationRepository repository;
    private final JpaReferenceResolver resolver;

    public InventoryReservationJpaAdapter(SpringDataJpaInventoryReservationRepository repository,
                                          JpaReferenceResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    public List<InventoryReservation> findByOrder(UUID orderId) {
        Map<String, Inventory> loaded = new HashMap<>();
        return repository.findByOrderId(orderId.toString()).stream()
                .map(entity -> InventoryReservationJpaMapper.toDomain(entity,
                        loaded.computeIfAbsent(entity.getInventoryId(), resolver::inventory)))
                .toList();
    }

    @Override
    public void saveAll(List<InventoryReservation> reservations) {
        repository.saveAll(reservations.stream().map(InventoryReservationJpaMapper::toEntity).toList());
    }
}