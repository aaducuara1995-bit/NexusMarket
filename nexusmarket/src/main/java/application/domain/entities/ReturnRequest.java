package application.domain.entities;

import application.domain.enums.OrderStatus;
import application.domain.enums.ReturnStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReturnRequest {

    private final UUID id;
    private final Order order;
    private final String reason;
    private ReturnStatus status;
    private final Map<UUID, Integer> restockedUnits = new HashMap<>();

    public ReturnRequest(Order order, String reason) {
        if (order == null)
            throw new IllegalArgumentException("The return must be linked to an order.");
        if (reason == null || reason.isBlank())
            throw new IllegalArgumentException("A return reason is required.");
        if (order.getStatus() != OrderStatus.DELIVERED)
            throw new IllegalStateException("Only a delivered order can be returned.");

        this.id = UUID.randomUUID();
        this.order = order;
        this.reason = reason.trim();
        this.status = ReturnStatus.REQUESTED;
    }

    public void approve() {
        requireRequested();
        status = ReturnStatus.APPROVED;
    }

    public void reject() {
        requireRequested();
        status = ReturnStatus.REJECTED;
    }

    private void requireRequested() {
        if (status != ReturnStatus.REQUESTED)
            throw new IllegalStateException("The return has already been resolved.");
    }

    public boolean isApproved() {
        return status == ReturnStatus.APPROVED;
    }

    /** A requested or approved return prevents opening another one for the same order. */
    public boolean isOpen() {
        return status != ReturnStatus.REJECTED;
    }

    public int purchasedUnits(UUID productId) {
        return order.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public int restockedUnits(UUID productId) {
        return restockedUnits.getOrDefault(productId, 0);
    }

    public int unitsPendingRestock(UUID productId) {
        return purchasedUnits(productId) - restockedUnits(productId);
    }

    public void registerRestock(UUID productId, int units) {
        if (status != ReturnStatus.APPROVED)
            throw new IllegalStateException("Restock can only be registered for an approved return.");
        if (units <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        if (purchasedUnits(productId) == 0)
            throw new IllegalArgumentException("The product does not belong to the returned order.");
        if (units > unitsPendingRestock(productId))
            throw new IllegalArgumentException(
                    "Cannot restock more units than were purchased in the order.");

        restockedUnits.merge(productId, units, Integer::sum);
    }

    public UUID getId() { return id; }
    public Order getOrder() { return order; }
    public String getReason() { return reason; }
    public ReturnStatus getStatus() { return status; }
}