package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data JPA repository for ShipmentJpaEntity. Query methods are derived from their names. */
public interface SpringDataJpaShipmentRepository extends JpaRepository<ShipmentJpaEntity, String> {

    Optional<ShipmentJpaEntity> findByOrderId(String orderId);
}