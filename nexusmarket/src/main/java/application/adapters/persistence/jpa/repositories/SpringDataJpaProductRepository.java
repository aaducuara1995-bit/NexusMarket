package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.domain.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data JPA repository for ProductJpaEntity. Query methods are derived from their names. */
public interface SpringDataJpaProductRepository extends JpaRepository<ProductJpaEntity, String> {

    boolean existsBySellerIdAndNameIgnoreCase(String sellerId, String name);

    List<ProductJpaEntity> findBySellerId(String sellerId);

    List<ProductJpaEntity> findByStatus(ProductStatus status);
}