package application.domain.services;

import application.domain.entities.InventoryMovement;
import application.domain.entities.InventoryReservation;
import application.domain.enums.MovementType;

import java.util.ArrayList;
import java.util.List;

public class ReleaseReservationService {

    public List<InventoryMovement> release(List<InventoryReservation> reservations) {
        if (reservations == null)
            throw new IllegalArgumentException("Reservation list is required.");

        // Everything is validated before any change, so nothing is released halfway.
        for (InventoryReservation reservation : reservations) {
            if (!reservation.isActive())
                throw new IllegalStateException("Only active reservations can be released.");
        }

        List<InventoryMovement> movements = new ArrayList<>();
        for (InventoryReservation reservation : reservations) {
            reservation.release();
            movements.add(new InventoryMovement(
                    reservation.getInventory(), MovementType.RESERVATION,
                    -reservation.getQuantity(),
                    "Release of reservation for order " + reservation.getOrderId()));
        }
        return movements;
    }
}