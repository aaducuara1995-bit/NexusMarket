package application.domain.ports.in;

import application.domain.entities.Inventory;
import application.domain.entities.Seller;
import application.domain.entities.User;

/** Input port: marks an inventory as damaged, blocks it or enables it again. */
public interface MarkInventoryUseCase {

    void markDamaged(User actor, Seller seller, Inventory inventory);

    void block(User actor, Seller seller, Inventory inventory);

    void enable(User actor, Seller seller, Inventory inventory);
}