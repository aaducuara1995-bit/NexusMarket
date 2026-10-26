package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data JPA repository for InventoryJpaEntity. Query methods are derived from their names. */
public interface SpringDataJpaInventoryRepository extends JpaRepository<InventoryJpaEntity, String> {

    List<InventoryJpaEntity> findByProductId(String productId);
}