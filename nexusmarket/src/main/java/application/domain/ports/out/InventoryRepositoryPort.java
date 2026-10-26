package application.domain.ports.out;

import application.domain.entities.Inventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepositoryPort {

    Optional<Inventory> findById(UUID id);

    List<Inventory> findByProduct(UUID productId);

    List<Inventory> findAll();

    void save(Inventory inventory);

    void saveAll(List<Inventory> inventories);
}