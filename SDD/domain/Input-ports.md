# Input Ports

## Introducción

Los puertos de entrada son los contratos con los que el exterior (por ejemplo, un controlador REST) usa el dominio. Están en `domain/ports/in/` y se organizan por rol: un puerto por cada rol de `UserRole`, más un puerto de acceso público para lo que no requiere iniciar sesión.

Así cada rol ve solo las operaciones que tiene permitidas, y la capa REST y la seguridad pueden validar el rol contra el puerto que se invoca.

---

# Principios

* **Aislamiento por rol.** Cada puerto agrupa las operaciones de un solo rol. Si dos roles comparten una operación (por ejemplo, el inventario del vendedor y del operador logístico), aparece en ambos puertos.
* **Parámetros del dominio.** Los métodos reciben entidades y objetos de valor del dominio, nunca DTOs. El primer parámetro es el `User` que hace la petición, reconstruido a partir del token JWT.
* **Implementación en adaptadores.** Cada puerto lo implementa una clase de `adapters/useCases/` (ver `../Adapters/Use-cases-adapters.md`). Los servicios de dominio ya no implementan puertos: son las piezas que esas clases inyectan.

---

# Definición de los puertos

## 1. PublicAccessPort

Operaciones disponibles sin iniciar sesión: el login, el registro de compradores y la consulta del catálogo.

**Rol:** Público (sin autenticación)  
**Implementado por:** `PublicAccessUseCaseImpl`

```java
public interface PublicAccessPort {
    AuthenticationResult login(Email email, String password);
    Buyer registerBuyer(User user, Address mainAddress, String password);
    List<Product> consultCatalog();
    int consultAvailability(Product product);
}
```

## 2. BuyerPort

Carrito, confirmación y pago de pedidos, consulta de sus pedidos y solicitud de devoluciones.

**Rol:** `BUYER`  
**Implementado por:** `BuyerUseCaseImpl`

```java
public interface BuyerPort {
    Cart consultMyCart(User user);
    Cart addToCart(User user, Product product, int quantity);
    Cart removeFromCart(User user, Product product);
    Order confirmOrder(User user, Address deliveryAddress);
    Order payOrder(User user, Order order, Money paidAmount);
    List<Order> consultMyOrders(User user);
    ReturnRequest requestReturn(User user, Order order, String reason);
}
```

## 3. SellerPort

Catálogo e inventario de los productos propios del vendedor.

**Rol:** `SELLER`  
**Implementado por:** `SellerUseCaseImpl`

```java
public interface SellerPort {
    Product registerProduct(User user, Product product);
    Product publishProduct(User user, Product product);
    Product suspendProduct(User user, Product product);
    Product discontinueProduct(User user, Product product);
    List<Product> consultMyProducts(User user);
    Inventory registerInitialInventory(User user, Product product, Warehouse warehouse, int quantity);
    Inventory adjustInventory(User user, Inventory inventory, int difference, String reason);
    Inventory markInventoryDamaged(User user, Inventory inventory);
    Inventory blockInventory(User user, Inventory inventory);
    Inventory enableInventory(User user, Inventory inventory);
    Inventory restockFromReturn(User user, Inventory inventory, ReturnRequest returnRequest, int units);
}
```

## 4. LogisticOperatorPort

Operación de bodegas (inventario de cualquier vendedor) y despacho y entrega de pedidos.

**Rol:** `LOGISTIC_OPERATOR`  
**Implementado por:** `LogisticOperatorUseCaseImpl`

```java
public interface LogisticOperatorPort {
    Inventory registerInitialInventory(User user, Product product, Warehouse warehouse, int quantity);
    Inventory adjustInventory(User user, Inventory inventory, int difference, String reason);
    Inventory markInventoryDamaged(User user, Inventory inventory);
    Inventory blockInventory(User user, Inventory inventory);
    Inventory enableInventory(User user, Inventory inventory);
    Inventory restockFromReturn(User user, Inventory inventory, ReturnRequest returnRequest, int units);
    List<Order> consultOrdersToShip(User user);
    Order shipOrder(User user, Order order);
    Order confirmDelivery(User user, Order order);
}
```

## 5. AdminPort

Usuarios, alta de vendedores, bodegas y decisiones de posventa (devoluciones y reembolsos).

**Rol:** `ADMIN`  
**Implementado por:** `AdminUseCaseImpl`

```java
public interface AdminPort {
    User registerUser(User admin, User newUser, String password);
    User blockUser(User admin, User target);
    User activateUser(User admin, User target);
    Seller onboardSeller(User admin, User sellerUser, Warehouse firstWarehouse);
    Warehouse registerMarketplaceWarehouse(User admin, Warehouse warehouse);
    Warehouse registerSellerWarehouse(User admin, Seller seller, Warehouse warehouse);
    ReturnRequest approveReturn(User admin, ReturnRequest returnRequest);
    ReturnRequest rejectReturn(User admin, ReturnRequest returnRequest);
    Refund processRefund(User admin, Refund refund);
    List<AuditLog> consultAuditLog(User admin);
}
```

## 6. SupervisorPort

Consultas de seguimiento sin modificar datos (OBJ-12).

**Rol:** `SUPERVISOR`  
**Implementado por:** `SupervisorUseCaseImpl`

```java
public interface SupervisorPort {
    List<User> consultUsers(User supervisor);
    List<Warehouse> consultWarehouses(User supervisor);
    List<Inventory> consultInventory(User supervisor);
    List<Order> consultOrders(User supervisor);
    List<AuditLog> consultAuditLog(User supervisor);
    List<AuditLog> consultAuditLogOf(User supervisor, UUID entityId);
}
```