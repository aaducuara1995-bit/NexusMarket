package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.InventoryReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data JPA repository for InventoryReservationJpaEntity. Query methods are derived from their names. */
public interface SpringDataJpaInventoryReservationRepository extends JpaRepository<InventoryReservationJpaEntity, String> {

    List<InventoryReservationJpaEntity> findByOrderId(String orderId);
}