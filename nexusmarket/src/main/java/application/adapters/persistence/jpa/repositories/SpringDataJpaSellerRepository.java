package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.SellerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data JPA repository for SellerJpaEntity. Query methods are derived from their names. */
public interface SpringDataJpaSellerRepository extends JpaRepository<SellerJpaEntity, String> {

    boolean existsByUserId(String userId);

    Optional<SellerJpaEntity> findByUserId(String userId);
}