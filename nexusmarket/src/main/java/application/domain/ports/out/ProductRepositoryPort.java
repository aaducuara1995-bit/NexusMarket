package application.domain.ports.out;

import application.domain.entities.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryPort {

    boolean existsNameForSeller(UUID sellerId, String name);

    Optional<Product> findById(UUID id);

    List<Product> findBySeller(UUID sellerId);

    List<Product> findPublished();

    void save(Product product);
}