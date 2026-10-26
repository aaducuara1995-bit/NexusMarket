package application.domain.ports.out;

import application.domain.entities.Seller;

import java.util.Optional;
import java.util.UUID;

public interface SellerRepositoryPort {

    boolean existsByUserId(UUID userId);

    Optional<Seller> findById(UUID id);

    Optional<Seller> findByUserId(UUID userId);

    void save(Seller seller);
}