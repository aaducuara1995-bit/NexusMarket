package application.domain.ports.out;

import application.domain.entities.Buyer;

import java.util.Optional;
import java.util.UUID;

public interface BuyerRepositoryPort {

    boolean existsByUserId(UUID userId);

    Optional<Buyer> findById(UUID id);

    Optional<Buyer> findByUserId(UUID userId);

    void save(Buyer buyer);
}