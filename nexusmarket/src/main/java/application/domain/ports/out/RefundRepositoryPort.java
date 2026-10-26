package application.domain.ports.out;

import application.domain.entities.Refund;

import java.util.Optional;
import java.util.UUID;

public interface RefundRepositoryPort {

    Optional<Refund> findById(UUID id);

    Optional<Refund> findByOrder(UUID orderId);

    void save(Refund refund);
}