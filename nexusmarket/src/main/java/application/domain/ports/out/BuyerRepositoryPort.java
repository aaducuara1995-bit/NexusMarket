package application.domain.ports.out;

import application.domain.entities.Buyer;

import java.util.UUID;

public interface BuyerRepositoryPort {

    boolean existsByUserId(UUID userId);

    void save(Buyer buyer);
}