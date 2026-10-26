package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data JPA repository for UserJpaEntity. Query methods are derived from their names. */
public interface SpringDataJpaUserRepository extends JpaRepository<UserJpaEntity, String> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByDocument(String document);
}