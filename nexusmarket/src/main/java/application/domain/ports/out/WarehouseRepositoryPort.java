package application.domain.ports.out;

import application.domain.entities.Warehouse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepositoryPort {

    boolean existsByName(String name);

    Optional<Warehouse> findById(UUID id);

    List<Warehouse> findAll();

    void save(Warehouse warehouse);
}