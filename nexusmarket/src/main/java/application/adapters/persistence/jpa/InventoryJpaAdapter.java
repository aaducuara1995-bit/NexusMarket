package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.InventoryJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInventoryRepository;
import application.domain.entities.Inventory;
import application.domain.ports.out.InventoryRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output persistence adapter for Inventory backed by Spring Data JPA (MySQL).
 * Product and warehouse are rebuilt through JpaReferenceResolver.
 */
@Repository
public class InventoryJpaAdapter implements InventoryRepositoryPort {

    private final SpringDataJpaInventoryRepository repository;
    private final JpaReferenceResolver resolver;

    public InventoryJpaAdapter(SpringDataJpaInventoryRepository repository,
                               JpaReferenceResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    public Optional<Inventory> findById(UUID id) {
        return repository.findById(id.toString()).map(resolver::inventory);
    }

    @Override
    public List<Inventory> findByProduct(UUID productId) {
        return repository.findByProductId(productId.toString()).stream().map(resolver::inventory).toList();
    }

    @Override
    public List<Inventory> findAll() {
        return repository.findAll().stream().map(resolver::inventory).toList();
    }

    @Override
    public void save(Inventory inventory) {
        repository.save(InventoryJpaMapper.toEntity(inventory));
    }

    @Override
    public void saveAll(List<Inventory> inventories) {
        repository.saveAll(inventories.stream().map(InventoryJpaMapper::toEntity).toList());
    }
}