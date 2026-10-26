package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Order;
import application.domain.entities.User;
import application.domain.exceptions.AccessDeniedException;
import application.domain.valueobjects.Money;

public class PayOrderService {

    private final BuyerValidationService buyerValidation;

    public PayOrderService(BuyerValidationService buyerValidation) {
        if (buyerValidation == null)
            throw new IllegalArgumentException("Validation service is required.");
        this.buyerValidation = buyerValidation;
    }

    public void pay(User user, Buyer buyer, Order order, Money paidAmount) {
        buyerValidation.requireEnabled(user, buyer);
        if (order == null)
            throw new IllegalArgumentException("Order is required.");
        if (paidAmount == null)
            throw new IllegalArgumentException("Paid amount is required.");
        if (!order.belongsTo(buyer.getId()))
            throw new AccessDeniedException("The order belongs to another buyer.");

        Money total = order.calculateTotal();
        boolean matches = total.currency().equals(paidAmount.currency())
                && total.amount().compareTo(paidAmount.amount()) == 0;
        if (!matches)
            throw new IllegalArgumentException("The paid amount does not match the order total.");

        order.pay();
    }
}