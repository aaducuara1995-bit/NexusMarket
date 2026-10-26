package application.domain.ports.in;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Inventory;
import application.domain.entities.User;
import application.domain.services.ConfirmOrderService;

import java.util.List;

/** Input port: the buyer turns their cart into an order and stock is reserved. */
public interface ConfirmOrderUseCase {

    ConfirmOrderService.Result confirm(User user, Buyer buyer, Cart cart,
                                                  List<Inventory> inventories);
}