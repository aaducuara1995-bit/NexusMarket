package application.domain.ports.in;

import application.domain.entities.AuditLog;
import application.domain.entities.Inventory;
import application.domain.entities.Order;
import application.domain.entities.User;
import application.domain.entities.Warehouse;

import java.util.List;
import java.util.UUID;

/** Read-only follow-up operations of the SUPERVISOR role (OBJ-12). */
public interface SupervisorPort {

    List<User> consultUsers(User supervisor);

    List<Warehouse> consultWarehouses(User supervisor);

    List<Inventory> consultInventory(User supervisor);

    List<Order> consultOrders(User supervisor);

    List<AuditLog> consultAuditLog(User supervisor);

    List<AuditLog> consultAuditLogOf(User supervisor, UUID entityId);
}