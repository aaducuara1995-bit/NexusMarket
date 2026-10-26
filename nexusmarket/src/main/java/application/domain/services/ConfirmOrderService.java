package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.CartItem;
import application.domain.entities.Inventory;
import application.domain.entities.InventoryMovement;
import application.domain.entities.InventoryReservation;
import application.domain.entities.Order;
import application.domain.entities.User;
import application.domain.exceptions.AccessDeniedException;
import application.domain.valueobjects.Address;

import java.util.ArrayList;
import java.util.List;

public class ConfirmOrderService {

    public record Result(Order order,
                            List<InventoryReservation> reservations,
                            List<InventoryMovement> movements) {
    }

    private final BuyerValidationService buyerValidation;
    private final ReserveInventoryService reservationService;
    private final ReleaseReservationService releaseService;

    public ConfirmOrderService(BuyerValidationService buyerValidation,
                                     ReserveInventoryService reservationService,
                                     ReleaseReservationService releaseService) {
        if (buyerValidation == null || reservationService == null || releaseService == null)
            throw new IllegalArgumentException("Supporting services are required.");
        this.buyerValidation = buyerValidation;
        this.reservationService = reservationService;
        this.releaseService = releaseService;
    }

    public Result confirm(User user, Buyer buyer, Cart cart,
                               List<Inventory> inventories) {
        return confirm(user, buyer, cart, inventories, null);
    }

    /** Confirms the cart using the delivery address chosen by the buyer (main address if null). */
    public Result confirm(User user, Buyer buyer, Cart cart,
                          List<Inventory> inventories, Address deliveryAddress) {
        buyerValidation.requireEnabled(user, buyer);
        if (cart == null)
            throw new IllegalArgumentException("Cart is required.");
        if (inventories == null)
            throw new IllegalArgumentException("Inventory list is required.");
        if (!cart.getBuyer().getId().equals(buyer.getId()))
            throw new AccessDeniedException("The cart belongs to another buyer.");
        if (cart.getItems().isEmpty())
            throw new IllegalArgumentException("Cannot confirm an empty cart.");

        for (CartItem item : cart.getItems()) {
            if (!item.getProduct().isPublished())
                throw new IllegalStateException(
                        "Product " + item.getProduct().getName() + " is no longer published.");
        }

        Order order = new Order(cart, deliveryAddress);
        List<InventoryReservation> reservations = new ArrayList<>();
        List<InventoryMovement> movements = new ArrayList<>();

        try {
            for (CartItem item : cart.getItems()) {
                var result = reservationService.reserve(
                        order.getId(), item.getProduct(), item.getQuantity(), inventories);
                reservations.addAll(result.reservations());
                movements.addAll(result.movements());
            }
        } catch (RuntimeException e) {
            // All or nothing: if one product fails, the reservations already made are undone.
            releaseService.release(reservations);
            throw e;
        }

        cart.clear();
        return new Result(order, reservations, movements);
    }
}