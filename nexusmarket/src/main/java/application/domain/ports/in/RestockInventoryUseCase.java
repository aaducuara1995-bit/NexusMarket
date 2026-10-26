package application.domain.ports.in;

import application.domain.entities.Inventory;
import application.domain.entities.InventoryMovement;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;

/** Input port: returns the units of an approved return to inventory. */
public interface RestockInventoryUseCase {

    InventoryMovement restock(User actor, Seller seller, Inventory inventory,
                                    int units, ReturnRequest returnRequest);
}