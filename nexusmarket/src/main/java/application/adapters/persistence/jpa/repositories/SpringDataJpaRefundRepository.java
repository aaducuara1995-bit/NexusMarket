package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.RefundJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data JPA repository for RefundJpaEntity. Query methods are derived from their names. */
public interface SpringDataJpaRefundRepository extends JpaRepository<RefundJpaEntity, String> {

    Optional<RefundJpaEntity> findByOrderId(String orderId);
}