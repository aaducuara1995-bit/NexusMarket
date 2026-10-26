package application.domain.ports.in;

import application.domain.entities.Buyer;
import application.domain.entities.Order;
import application.domain.entities.User;
import application.domain.valueobjects.Money;

/** Input port: the buyer pays the total of their order. */
public interface PayOrderUseCase {

    void pay(User user, Buyer buyer, Order order, Money paidAmount);
}