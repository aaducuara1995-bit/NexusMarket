package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.BuyerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data JPA repository for BuyerJpaEntity. Query methods are derived from their names. */
public interface SpringDataJpaBuyerRepository extends JpaRepository<BuyerJpaEntity, String> {

    boolean existsByUserId(String userId);

    Optional<BuyerJpaEntity> findByUserId(String userId);
}