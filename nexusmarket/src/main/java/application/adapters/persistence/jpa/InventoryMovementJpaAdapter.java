package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.InventoryMovementJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInventoryMovementRepository;
import application.domain.entities.InventoryMovement;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Output persistence adapter for InventoryMovement backed by Spring Data JPA (MySQL).
 * Movements are only inserted, never updated.
 */
@Repository
public class InventoryMovementJpaAdapter implements InventoryMovementRepositoryPort {

    private final SpringDataJpaInventoryMovementRepository repository;

    public InventoryMovementJpaAdapter(SpringDataJpaInventoryMovementRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<InventoryMovement> findByInventory(UUID inventoryId) {
        return repository.findByInventoryId(inventoryId.toString()).stream()
                .map(InventoryMovementJpaMapper::toDomain).toList();
    }

    @Override
    public void saveAll(List<InventoryMovement> movements) {
        repository.saveAll(movements.stream().map(InventoryMovementJpaMapper::toEntity).toList());
    }
}