package application.domain.ports.out;

import application.domain.entities.Product;

import java.util.UUID;

public interface ProductRepositoryPort {

    boolean existsNameForSeller(UUID sellerId, String name);

    void save(Product product);
}