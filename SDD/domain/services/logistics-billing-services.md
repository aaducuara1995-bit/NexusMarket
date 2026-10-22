# Facturación y logística

Factura, envío, despacho y confirmación de entrega (OBJ-09 y OBJ-10).

---

## ConfirmDeliveryService

**Propósito:** Confirma la entrega y finaliza el pedido.

**Quién lo ejecuta:** Operador logístico.

**Puerto de entrada que implementa:** `ConfirmDeliveryUseCase`

**Dependencias:** `AuthorizationService authorization`

**Operaciones:**

```java
void confirm(User actor, Order order, Shipment shipment);
```

**Reglas de negocio:**

* El envío debe estar despachado.
* El envío pasa a `DELIVERED` y el pedido queda finalizado.

---

## CreateShipmentService

**Propósito:** Crea el envío de un pedido pagado con productos físicos.

**Quién lo ejecuta:** Interno (después del pago).

**Puerto de entrada que implementa:** `CreateShipmentUseCase`

**Dependencias:** ninguna

**Operaciones:**

```java
Shipment create(Order order, Address deliveryAddress, List<Shipment> existing);
```

**Reglas de negocio:**

* Los pedidos solo digitales no llevan envío.
* La dirección de entrega debe ser del comprador.
* Un pedido tiene un solo envío.

---

## IssueInvoiceService

**Propósito:** Emite la factura de un pedido pagado.

**Quién lo ejecuta:** Interno (después del pago).

**Puerto de entrada que implementa:** `IssueInvoiceUseCase`

**Dependencias:** ninguna

**Operaciones:**

```java
Invoice issue(Order order, List<Invoice> existing);
```

**Reglas de negocio:**

* Solo se factura un pedido pagado.
* Un pedido no se factura dos veces.

---

## ShipOrderService

**Propósito:** Despacha un pedido: confirma la salida de inventario y actualiza envío y pedido a la vez.

**Quién lo ejecuta:** Operador logístico.

**Puerto de entrada que implementa:** `ShipOrderUseCase`

**Dependencias:** `AuthorizationService authorization`, `InventoryOutflowService inventoryOutflow`

**Operaciones:**

```java
List<InventoryMovement> ship(User actor, Order order, Shipment shipment, List<InventoryReservation> reservations);
```

**Reglas de negocio:**

* Requiere permiso de `SHIPPING_OPERATIONS`.
* El envío debe corresponder al pedido, el pedido estar pagado y el envío pendiente.
* Valida todo antes de modificar algo.
* El pedido y el envío pasan a `SHIPPED`.
