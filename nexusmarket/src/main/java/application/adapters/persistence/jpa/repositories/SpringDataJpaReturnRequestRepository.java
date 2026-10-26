package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.ReturnRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data JPA repository for ReturnRequestJpaEntity. Query methods are derived from their names. */
public interface SpringDataJpaReturnRequestRepository extends JpaRepository<ReturnRequestJpaEntity, String> {

    List<ReturnRequestJpaEntity> findByOrderId(String orderId);
}