package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;


/** Spring Data JPA repository for WarehouseJpaEntity. Query methods are derived from their names. */
public interface SpringDataJpaWarehouseRepository extends JpaRepository<WarehouseJpaEntity, String> {

    boolean existsByNameIgnoreCase(String name);
}