# Carrito y pedidos

Desde la selección de productos hasta el pago y la entrega de productos digitales (OBJ-07, OBJ-08 y Dominio 7).

---

## AddToCartService

**Propósito:** Agrega un producto al carrito del comprador.

**Quién lo ejecuta:** Comprador.

**Lo usa:** `BuyerUseCaseImpl`

**Dependencias:** `BuyerValidationService buyerValidation`

**Operaciones:**

```java
void add(User user, Buyer buyer, Cart cart, Product product, int quantity, List<Inventory> inventories);
```

**Reglas de negocio:**

* El comprador debe estar habilitado y el carrito ser suyo.
* Solo se agregan productos publicados.
* La cantidad total pedida no puede superar las existencias.
* No reserva stock.

---

## ConfirmOrderService

**Propósito:** Convierte el carrito en un pedido y reserva el stock de cada producto.

**Quién lo ejecuta:** Comprador.

**Lo usa:** `BuyerUseCaseImpl`

**Dependencias:** `BuyerValidationService buyerValidation`, `ReserveInventoryService reservationService`, `ReleaseReservationService releaseService`

**Operaciones:**

```java
Result confirm(User user, Buyer buyer, Cart cart, List<Inventory> inventories);
Result confirm(User user, Buyer buyer, Cart cart, List<Inventory> inventories, Address deliveryAddress);
```

**Resultado:** `Result(Order order, List<InventoryReservation> reservations, List<InventoryMovement> movements)`

**Reglas de negocio:**

* El carrito no puede estar vacío y sus productos deben seguir publicados.
* Todo o nada: si un producto no alcanza, se deshacen las reservas y el carrito queda intacto.
* Si todo sale bien, el carrito se vacía y el pedido queda `PENDING_PAYMENT`.
* Recibe la dirección de entrega que elige el comprador; si no llega, el pedido usa la dirección principal. Una dirección que no es del comprador se rechaza antes de reservar.

---

## DeliverDigitalOrderService

**Propósito:** Entrega de inmediato un pedido pagado formado solo por productos digitales.

**Quién lo ejecuta:** Interno (después del pago).

**Lo usa:** `BuyerUseCaseImpl`

**Dependencias:** ninguna

**Operaciones:**

```java
void deliver(Order order);
```

**Reglas de negocio:**

* Rechaza pedidos con productos físicos: esos siguen por logística.
* El pedido pasa a `DELIVERED`.

---

## PayOrderService

**Propósito:** Registra el pago de un pedido.

**Quién lo ejecuta:** Comprador dueño del pedido.

**Lo usa:** `BuyerUseCaseImpl`

**Dependencias:** `BuyerValidationService buyerValidation`

**Operaciones:**

```java
void pay(User user, Buyer buyer, Order order, Money paidAmount);
```

**Reglas de negocio:**

* El monto y la moneda deben coincidir exactamente con el total.
* Un pedido se paga una sola vez.