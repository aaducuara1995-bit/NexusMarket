package application.domain.services;

import application.domain.entities.InventoryMovement;
import application.domain.entities.InventoryReservation;
import application.domain.enums.MovementType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InventoryOutflowService {

    public List<InventoryMovement> confirmOutflow(UUID orderId,
                                                      List<InventoryReservation> reservations) {
        if (orderId == null)
            throw new IllegalArgumentException("Order is required.");
        if (reservations == null)
            throw new IllegalArgumentException("Reservation list is required.");

        // Everything is validated before any change, so nothing is confirmed halfway.
        for (InventoryReservation reservation : reservations) {
            if (!reservation.belongsToOrder(orderId))
                throw new IllegalArgumentException("Some reservations do not belong to the order.");
            if (!reservation.isActive())
                throw new IllegalStateException("Only active reservations can be shipped out.");
        }

        List<InventoryMovement> movements = new ArrayList<>();
        for (InventoryReservation reservation : reservations) {
            reservation.confirmOutflow();
            movements.add(new InventoryMovement(
                    reservation.getInventory(), MovementType.SALE,
                    -reservation.getQuantity(),
                    "Sale outflow for order " + orderId));
        }
        return movements;
    }
}