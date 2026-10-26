package application.domain.ports.out;

import application.domain.entities.InventoryReservation;

import java.util.List;
import java.util.UUID;

public interface InventoryReservationRepositoryPort {

    List<InventoryReservation> findByOrder(UUID orderId);

    void saveAll(List<InventoryReservation> reservations);
}