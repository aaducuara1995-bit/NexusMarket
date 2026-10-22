# Input Ports

## Introducción

Los puertos de entrada son los contratos con los que el exterior (por ejemplo, un controlador REST) usa el dominio. Están en `domain/ports/in/` y trabajan solo con entidades y objetos de valor del dominio, nunca con DTOs.

En esta etapa hay un puerto por caso de uso, y cada uno lo implementa el servicio de dominio correspondiente. En la etapa de adaptadores se agruparán por rol (comprador, vendedor, operador logístico, administrador, supervisor y acceso público), con clases de caso de uso que inyecten estos servicios.

Los servicios internos de validación y de reservas no tienen puerto de entrada, porque solo los usan otros servicios. Hay 25 puertos.

---

## AddToCartUseCase

Agrega un producto al carrito del comprador.

**Implementado por:** `AddToCartService`  
**Rol:** Comprador.

```java
void add(User user, Buyer buyer, Cart cart, Product product, int quantity, List<Inventory> inventories);
```

## AdjustInventoryUseCase

Ajusta las existencias de un inventario con un motivo.

**Implementado por:** `AdjustInventoryService`  
**Rol:** Vendedor u operador logístico.

```java
InventoryMovement adjust(User actor, Seller seller, Inventory inventory, int difference, String reason);
```

## ChangeProductStatusUseCase

Suspende o descontinúa un producto.

**Implementado por:** `ChangeProductStatusService`  
**Rol:** Vendedor dueño del producto.

```java
void suspend(User actor, Seller seller, Product product);
void discontinue(User actor, Seller seller, Product product, List<Inventory> inventories);
```

## CheckAvailabilityUseCase

Calcula cuántas unidades de un producto hay disponibles entre todas las bodegas.

**Implementado por:** `CheckAvailabilityService`  
**Rol:** Cualquier usuario (consulta).

```java
int availableOf(Product product, List<Inventory> inventories);
boolean hasAvailability(Product product, int quantity, List<Inventory> inventories);
```

## ConfirmDeliveryUseCase

Confirma la entrega y finaliza el pedido.

**Implementado por:** `ConfirmDeliveryService`  
**Rol:** Operador logístico.

```java
void confirm(User actor, Order order, Shipment shipment);
```

## ConfirmOrderUseCase

Convierte el carrito en un pedido y reserva el stock de cada producto.

**Implementado por:** `ConfirmOrderService`  
**Rol:** Comprador.

```java
ConfirmOrderService.Result confirm(User user, Buyer buyer, Cart cart, List<Inventory> inventories);
```

## CreateShipmentUseCase

Crea el envío de un pedido pagado con productos físicos.

**Implementado por:** `CreateShipmentService`  
**Rol:** Interno (después del pago).

```java
Shipment create(Order order, Address deliveryAddress, List<Shipment> existing);
```

## DeliverDigitalOrderUseCase

Entrega de inmediato un pedido pagado formado solo por productos digitales.

**Implementado por:** `DeliverDigitalOrderService`  
**Rol:** Interno (después del pago).

```java
void deliver(Order order);
```

## GenerateRefundUseCase

Genera el reembolso de una devolución aprobada.

**Implementado por:** `GenerateRefundService`  
**Rol:** Interno (después de aprobar).

```java
Refund generate(ReturnRequest returnRequest, List<Refund> existing);
```

## IssueInvoiceUseCase

Emite la factura de un pedido pagado.

**Implementado por:** `IssueInvoiceService`  
**Rol:** Interno (después del pago).

```java
Invoice issue(Order order, List<Invoice> existing);
```

## MarkInventoryUseCase

Marca un inventario como dañado, lo bloquea o lo habilita.

**Implementado por:** `MarkInventoryService`  
**Rol:** Vendedor u operador logístico.

```java
void markDamaged(User actor, Seller seller, Inventory inventory);
void block(User actor, Seller seller, Inventory inventory);
void enable(User actor, Seller seller, Inventory inventory);
```

## OnboardSellerUseCase

Incorpora a un vendedor junto con su primera bodega, como describe el flujo 6.1.

**Implementado por:** `OnboardSellerService`  
**Rol:** Administrador.

```java
OnboardSellerService.Result onboard(User admin, User sellerUser, String warehouseName, WarehouseType warehouseType, Address warehouseAddress);
```

## PayOrderUseCase

Registra el pago de un pedido.

**Implementado por:** `PayOrderService`  
**Rol:** Comprador dueño del pedido.

```java
void pay(User user, Buyer buyer, Order order, Money paidAmount);
```

## ProcessRefundUseCase

Procesa un reembolso pendiente.

**Implementado por:** `ProcessRefundService`  
**Rol:** Administrador.

```java
void process(User actor, Refund refund);
```

## PublishProductUseCase

Publica un producto en el catálogo.

**Implementado por:** `PublishProductService`  
**Rol:** Vendedor dueño del producto.

```java
void publish(User actor, Seller seller, Product product, List<Inventory> inventories);
```

## RegisterBuyerUseCase

Crea el perfil de comprador de un usuario.

**Implementado por:** `RegisterBuyerService`  
**Rol:** Comprador.

```java
Buyer register(User user, Address mainAddress);
```

## RegisterInitialInventoryUseCase

Registra las existencias iniciales de un producto en una bodega.

**Implementado por:** `RegisterInitialInventoryService`  
**Rol:** Vendedor u operador logístico.

```java
RegisterInitialInventoryService.Result register(User actor, Seller seller, Product product, Warehouse warehouse, int initialQuantity, List<Inventory> existing);
```

## RegisterProductUseCase

Registra un producto nuevo de un vendedor.

**Implementado por:** `RegisterProductService`  
**Rol:** Vendedor.

```java
Product register(User actor, Seller seller, String name, String description, ProductType type, Money price);
```

## RegisterSellerUseCase

Crea el perfil de vendedor de un usuario.

**Implementado por:** `RegisterSellerService`  
**Rol:** Administrador.

```java
Seller register(User admin, User sellerUser);
```

## RegisterUserUseCase

Registra un usuario nuevo.

**Implementado por:** `RegisterUserService`  
**Rol:** Comprador (registro propio) o administrador.

```java
User register(User actor, FullName name, Email email, IdentityDocument document, UserRole role);
```

## RegisterWarehouseUseCase

Registra bodegas del Marketplace o de un vendedor.

**Implementado por:** `RegisterWarehouseService`  
**Rol:** Administrador.

```java
Warehouse registerForMarketplace(User admin, String name, WarehouseType type, Address address);
Warehouse registerForSeller(User admin, Seller seller, String name, WarehouseType type, Address address);
```

## RequestReturnUseCase

Registra la solicitud de devolución de un pedido.

**Implementado por:** `RequestReturnService`  
**Rol:** Comprador dueño del pedido.

```java
ReturnRequest request(User user, Buyer buyer, Order order, String reason, List<ReturnRequest> existing);
```

## ResolveReturnUseCase

Aprueba o rechaza una devolución.

**Implementado por:** `ResolveReturnService`  
**Rol:** Administrador.

```java
void approve(User actor, ReturnRequest returnRequest);
void reject(User actor, ReturnRequest returnRequest);
```

## RestockInventoryUseCase

Reintegra al inventario las unidades de una devolución aprobada.

**Implementado por:** `RestockInventoryService`  
**Rol:** Vendedor u operador logístico.

```java
InventoryMovement restock(User actor, Seller seller, Inventory inventory, int units, ReturnRequest returnRequest);
```

## ShipOrderUseCase

Despacha un pedido: confirma la salida de inventario y actualiza envío y pedido a la vez.

**Implementado por:** `ShipOrderService`  
**Rol:** Operador logístico.

```java
List<InventoryMovement> ship(User actor, Order order, Shipment shipment, List<InventoryReservation> reservations);
```
