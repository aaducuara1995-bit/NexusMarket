package application.domain.ports.in;

import application.domain.entities.Cart;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.ReturnRequest;
import application.domain.entities.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Money;

import java.util.List;

/** Operations of the BUYER role. */
public interface BuyerPort {

    Cart consultMyCart(User user);

    Cart addToCart(User user, Product product, int quantity);

    Cart removeFromCart(User user, Product product);

    Order confirmOrder(User user, Address deliveryAddress);

    Order payOrder(User user, Order order, Money paidAmount);

    List<Order> consultMyOrders(User user);

    ReturnRequest requestReturn(User user, Order order, String reason);
}