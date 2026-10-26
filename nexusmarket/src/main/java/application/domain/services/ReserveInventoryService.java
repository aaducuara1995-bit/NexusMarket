package application.domain.services;

import application.domain.entities.Inventory;
import application.domain.entities.InventoryMovement;
import application.domain.entities.InventoryReservation;
import application.domain.entities.Product;
import application.domain.enums.InventoryStatus;
import application.domain.enums.MovementType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ReserveInventoryService {

    public record Result(List<InventoryReservation> reservations,
                            List<InventoryMovement> movements) {
    }

    public Result reserve(UUID orderId, Product product, int quantity,
                              List<Inventory> inventories) {
        if (orderId == null)
            throw new IllegalArgumentException("Order is required.");
        if (product == null)
            throw new IllegalArgumentException("Product is required.");
        if (inventories == null)
            throw new IllegalArgumentException("Inventory list is required.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero.");

        // Digital products do not consume inventory.
        if (!product.isPhysical())
            return new Result(List.of(), List.of());

        List<Inventory> candidates = inventories.stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .filter(i -> i.getStatus() == InventoryStatus.AVAILABLE)
                .filter(i -> i.getAvailable() > 0)
                .sorted(Comparator.comparingInt(Inventory::getAvailable).reversed())
                .toList();

        int totalAvailable = candidates.stream().mapToInt(Inventory::getAvailable).sum();
        if (totalAvailable < quantity)
            throw new IllegalStateException("Not enough stock of "
                    + product.getName() + " to reserve " + quantity + " units.");

        List<InventoryReservation> reservations = new ArrayList<>();
        List<InventoryMovement> movements = new ArrayList<>();
        int remaining = quantity;

        try {
            for (Inventory inventory : candidates) {
                if (remaining == 0)
                    break;

                int take = Math.min(remaining, inventory.getAvailable());
                reservations.add(InventoryReservation.create(orderId, inventory, take));
                movements.add(new InventoryMovement(
                        inventory, MovementType.RESERVATION, take,
                        "Reservation for order " + orderId));
                remaining -= take;
            }
        } catch (RuntimeException e) {
            reservations.forEach(InventoryReservation::release);
            throw e;
        }

        return new Result(reservations, movements);
    }
}