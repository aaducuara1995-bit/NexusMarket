package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.CartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data JPA repository for CartJpaEntity. Query methods are derived from their names. */
public interface SpringDataJpaCartRepository extends JpaRepository<CartJpaEntity, String> {

    Optional<CartJpaEntity> findByBuyerId(String buyerId);
}