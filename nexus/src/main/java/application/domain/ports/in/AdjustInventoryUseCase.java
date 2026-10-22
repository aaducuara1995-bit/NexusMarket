package application.domain.ports.in;

import application.domain.entities.Inventory;
import application.domain.entities.InventoryMovement;
import application.domain.entities.Seller;
import application.domain.entities.User;

/** Input port: raises or lowers an inventory's stock with a reason. */
public interface AdjustInventoryUseCase {

    InventoryMovement adjust(User actor, Seller seller, Inventory inventory,
                                 int difference, String reason);
}