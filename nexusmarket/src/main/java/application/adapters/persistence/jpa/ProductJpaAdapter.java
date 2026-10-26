package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.domain.entities.Product;
import application.domain.enums.ProductStatus;
import application.domain.ports.out.ProductRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Output persistence adapter for Product backed by Spring Data JPA (MySQL).  */
@Repository
public class ProductJpaAdapter implements ProductRepositoryPort {

    private final SpringDataJpaProductRepository repository;

    public ProductJpaAdapter(SpringDataJpaProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsNameForSeller(UUID sellerId, String name) {
        return repository.existsBySellerIdAndNameIgnoreCase(sellerId.toString(), name);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return repository.findById(id.toString()).map(ProductJpaMapper::toDomain);
    }

    @Override
    public List<Product> findBySeller(UUID sellerId) {
        return repository.findBySellerId(sellerId.toString()).stream().map(ProductJpaMapper::toDomain).toList();
    }

    @Override
    public List<Product> findPublished() {
        return repository.findByStatus(ProductStatus.PUBLISHED).stream().map(ProductJpaMapper::toDomain).toList();
    }

    @Override
    public void save(Product product) {
        repository.save(ProductJpaMapper.toEntity(product));
    }
}