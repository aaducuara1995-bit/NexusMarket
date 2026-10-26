package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaWarehouseRepository;
import application.domain.entities.Warehouse;
import application.domain.ports.out.WarehouseRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Output persistence adapter for Warehouse backed by Spring Data JPA (MySQL).  */
@Repository
public class WarehouseJpaAdapter implements WarehouseRepositoryPort {

    private final SpringDataJpaWarehouseRepository repository;

    public WarehouseJpaAdapter(SpringDataJpaWarehouseRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    @Override
    public Optional<Warehouse> findById(UUID id) {
        return repository.findById(id.toString()).map(WarehouseJpaMapper::toDomain);
    }

    @Override
    public List<Warehouse> findAll() {
        return repository.findAll().stream().map(WarehouseJpaMapper::toDomain).toList();
    }

    @Override
    public void save(Warehouse warehouse) {
        repository.save(WarehouseJpaMapper.toEntity(warehouse));
    }
}