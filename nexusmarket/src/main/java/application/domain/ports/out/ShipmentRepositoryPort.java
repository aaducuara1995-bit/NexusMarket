package application.domain.ports.out;

import application.domain.entities.Shipment;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepositoryPort {

    Optional<Shipment> findByOrder(UUID orderId);

    void save(Shipment shipment);
}