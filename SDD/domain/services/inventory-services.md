# Inventario

Existencias distribuidas por bodega y sus cinco movimientos: ingreso, reserva, salida por venta, ajuste y devolución (Dominio 6).

---

## AdjustInventoryService

**Propósito:** Ajusta las existencias de un inventario con un motivo.

**Quién lo ejecuta:** Vendedor u operador logístico.

**Lo usa:** `InventoryOperationsSupport`

**Dependencias:** `InventoryAccessService access`

**Operaciones:**

```java
InventoryMovement adjust(User actor, Seller seller, Inventory inventory, int difference, String reason);
```

**Reglas de negocio:**

* El motivo es obligatorio.
* No deja existencias negativas ni por debajo de lo reservado.
* Genera un movimiento `ADJUSTMENT`.

---

## InventoryOutflowService

**Propósito:** Convierte las reservas de un pedido en salida definitiva.

**Quién lo ejecuta:** Interno (lo usa `ShipOrderService`).

**Dependencias:** ninguna

**Operaciones:**

```java
List<InventoryMovement> confirmOutflow(UUID orderId, List<InventoryReservation> reservations);
```

**Reglas de negocio:**

* Todas las reservas deben ser del pedido y estar activas.
* Valida todas antes de confirmar ninguna.
* Genera movimientos `SALE`.

---

## MarkInventoryService

**Propósito:** Marca un inventario como dañado, lo bloquea o lo habilita.

**Quién lo ejecuta:** Vendedor u operador logístico.

**Lo usa:** `InventoryOperationsSupport`

**Dependencias:** `InventoryAccessService access`

**Operaciones:**

```java
void markDamaged(User actor, Seller seller, Inventory inventory);
void block(User actor, Seller seller, Inventory inventory);
void enable(User actor, Seller seller, Inventory inventory);
```

**Reglas de negocio:**

* No se marca como dañado un inventario con reservas activas.
* Solo se bloquea un inventario disponible y solo se habilita uno bloqueado.

---

## RegisterInitialInventoryService

**Propósito:** Registra las existencias iniciales de un producto en una bodega.

**Quién lo ejecuta:** Vendedor u operador logístico.

**Lo usa:** `InventoryOperationsSupport`

**Dependencias:** `InventoryAccessService access`

**Operaciones:**

```java
Result register(User actor, Seller seller, Product product, Warehouse warehouse, int initialQuantity, List<Inventory> existing);
```

**Resultado:** `Result(Inventory inventory, List<InventoryMovement> movements)`

**Reglas de negocio:**

* Solo los productos físicos llevan inventario.
* Un vendedor solo usa sus bodegas o las del Marketplace.
* No se duplica la pareja producto y bodega.
* Genera un movimiento `INFLOW`.

---

## ReleaseReservationService

**Propósito:** Deshace las reservas de un pedido que no se concreta.

**Quién lo ejecuta:** Interno.

**Dependencias:** ninguna

**Operaciones:**

```java
List<InventoryMovement> release(List<InventoryReservation> reservations);
```

**Reglas de negocio:**

* Solo libera reservas activas.
* Valida todas antes de liberar ninguna.
* Genera movimientos `RESERVATION` con cantidad negativa.

---

## ReserveInventoryService

**Propósito:** Aparta stock de un producto para un pedido, repartiéndolo entre varias bodegas si hace falta.

**Quién lo ejecuta:** Interno (lo usa `ConfirmOrderService`).

**Dependencias:** ninguna

**Operaciones:**

```java
Result reserve(UUID orderId, Product product, int quantity, List<Inventory> inventories);
```

**Resultado:** `Result(List<InventoryReservation> reservations, List<InventoryMovement> movements)`

**Reglas de negocio:**

* Toma primero de la bodega con más unidades.
* Todo o nada: si el total no alcanza, no reserva nada.
* No usa inventario dañado ni bloqueado.
* Los productos digitales no reservan inventario.
* Genera movimientos `RESERVATION`.

---

## RestockInventoryService

**Propósito:** Reintegra al inventario las unidades de una devolución aprobada.

**Quién lo ejecuta:** Vendedor u operador logístico.

**Lo usa:** `InventoryOperationsSupport`

**Dependencias:** `InventoryAccessService access`

**Operaciones:**

```java
InventoryMovement restock(User actor, Seller seller, Inventory inventory, int units, ReturnRequest returnRequest);
```

**Reglas de negocio:**

* La devolución debe estar aprobada.
* El producto debe estar en el pedido devuelto.
* Nunca se reintegran más unidades de las compradas, aunque se haga en varias partes.
* Genera un movimiento `RETURN`.