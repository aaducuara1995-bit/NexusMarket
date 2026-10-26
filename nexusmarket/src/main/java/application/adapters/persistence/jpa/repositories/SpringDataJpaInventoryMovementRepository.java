package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.InventoryMovementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data JPA repository for InventoryMovementJpaEntity. Query methods are derived from their names. */
public interface SpringDataJpaInventoryMovementRepository extends JpaRepository<InventoryMovementJpaEntity, String> {

    List<InventoryMovementJpaEntity> findByInventoryId(String inventoryId);
}