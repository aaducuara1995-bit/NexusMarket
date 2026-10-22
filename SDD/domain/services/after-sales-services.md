# Posventa

Devoluciones y reembolsos (OBJ-11).

---

## GenerateRefundService

**Propósito:** Genera el reembolso de una devolución aprobada.

**Quién lo ejecuta:** Interno (después de aprobar).

**Puerto de entrada que implementa:** `GenerateRefundUseCase`

**Dependencias:** ninguna

**Operaciones:**

```java
Refund generate(ReturnRequest returnRequest, List<Refund> existing);
```

**Reglas de negocio:**

* La devolución debe estar aprobada.
* Se reembolsa el total del pedido.
* Un pedido tiene un solo reembolso.

---

## ProcessRefundService

**Propósito:** Procesa un reembolso pendiente.

**Quién lo ejecuta:** Administrador.

**Puerto de entrada que implementa:** `ProcessRefundUseCase`

**Dependencias:** `AuthorizationService authorization`

**Operaciones:**

```java
void process(User actor, Refund refund);
```

**Reglas de negocio:**

* Requiere permiso de `REFUND_MANAGEMENT` y rol de administrador.
* Un reembolso se procesa una sola vez.

---

## RequestReturnService

**Propósito:** Registra la solicitud de devolución de un pedido.

**Quién lo ejecuta:** Comprador dueño del pedido.

**Puerto de entrada que implementa:** `RequestReturnUseCase`

**Dependencias:** `AuthorizationService authorization`

**Operaciones:**

```java
ReturnRequest request(User user, Buyer buyer, Order order, String reason, List<ReturnRequest> existing);
```

**Reglas de negocio:**

* Solo se devuelve un pedido entregado y con un motivo.
* No se abre otra devolución si hay una solicitada o aprobada; sí, si la anterior fue rechazada.

---

## ResolveReturnService

**Propósito:** Aprueba o rechaza una devolución.

**Quién lo ejecuta:** Administrador.

**Puerto de entrada que implementa:** `ResolveReturnUseCase`

**Dependencias:** `AuthorizationService authorization`

**Operaciones:**

```java
void approve(User actor, ReturnRequest returnRequest);
void reject(User actor, ReturnRequest returnRequest);
```

**Reglas de negocio:**

* El comprador no resuelve su propia devolución.
* Una devolución se resuelve una sola vez.
