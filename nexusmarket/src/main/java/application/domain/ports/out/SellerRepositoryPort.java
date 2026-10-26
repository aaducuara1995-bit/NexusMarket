package application.domain.ports.out;

import application.domain.entities.Seller;

import java.util.UUID;

public interface SellerRepositoryPort {

    boolean existsByUserId(UUID userId);

    void save(Seller seller);
}