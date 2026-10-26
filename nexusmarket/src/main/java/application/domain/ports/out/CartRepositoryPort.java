package application.domain.ports.out;

import application.domain.entities.Cart;

import java.util.Optional;
import java.util.UUID;

public interface CartRepositoryPort {

    Optional<Cart> findByBuyer(UUID buyerId);

    void save(Cart cart);
}