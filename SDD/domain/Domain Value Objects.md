# Domain Value Objects

## Introducción

Los objetos de valor representan conceptos del negocio que no tienen identidad propia: importan por su contenido. Son inmutables y dos objetos con los mismos datos se consideran iguales. Cada uno valida sus reglas al crearse, así que dentro del dominio nunca existe un correo mal escrito ni un monto negativo.

En el código se implementan como `record` de Java en `domain/valueobjects/`. Los estados, roles y tipos acotados del negocio se implementan como `enum` en `domain/enums/`, de modo que ningún campo acotado queda como `String` abierto.

---

# Jerarquía

```text
valueobjects
├── Address
├── Email
├── FullName
├── IdentityDocument
└── Money

enums
├── AuditOperation
├── BusinessProcess
├── CommercialStatus
├── InventoryStatus
├── MovementType
├── OrderStatus
├── ProductStatus
├── ProductType
├── RefundStatus
├── ReservationStatus
├── ReturnStatus
├── ShipmentStatus
├── UserRole
├── UserStatus
├── WarehouseOwnership
└── WarehouseType
```

---

# Objetos de valor

---

# Address

## Descripción

Ubicación física: direcciones del comprador, bodegas físicas y destino de los envíos.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `street` | `String` | Calle y número. |
| `city` | `String` | Ciudad. |
| `department` | `String` | Departamento. |
| `postalCode` | `String` | Código postal. |

## Reglas

* Todos los campos son obligatorios.

---

# Email

## Descripción

Correo electrónico de un usuario.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `value` | `String` | Dirección de correo. Se guarda en minúsculas y sin espacios. |

## Reglas

* No puede estar vacío.
* Debe tener un formato válido (`usuario@dominio.ext`).
* "Ana@Mail.com" y "ana@mail.com" son el mismo correo.

---

# FullName

## Descripción

Nombre de una persona registrada.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `firstNames` | `String` | Nombres. |
| `lastNames` | `String` | Apellidos. |

## Reglas

* Ambos campos son obligatorios.

---

# IdentityDocument

## Descripción

Documento de identidad de un usuario.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `value` | `String` | Número del documento. |

## Reglas

* No puede estar vacío.
* Solo admite dígitos.

---

# Money

## Descripción

Valor monetario: precios, subtotales, totales, pagos y reembolsos.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `amount` | `BigDecimal` | Monto, siempre con dos decimales. |
| `currency` | `String` | Código de moneda. `COP` por defecto. |

## Reglas

* El monto no puede ser negativo.
* La moneda es obligatoria y se guarda en mayúsculas.
* Fijar dos decimales permite comparar montos iguales sin errores de redondeo.

---

# Enumeraciones

---

# AuditOperation

## Descripción

Operaciones del negocio que dejan registro en la auditoría.

## Valores permitidos

| Valor | Significado |
|---|---|
| `USER_REGISTRATION` | Registro de un usuario. |
| `USER_BLOCKING` | Bloqueo de un usuario. |
| `USER_ACTIVATION` | Reactivación de un usuario. |
| `SELLER_ONBOARDING` | Alta de un vendedor con su primera bodega. |
| `WAREHOUSE_REGISTRATION` | Registro de una bodega. |
| `PRODUCT_REGISTRATION` | Registro de un producto. |
| `PRODUCT_STATUS_CHANGE` | Publicación, suspensión o descontinuación de un producto. |
| `INVENTORY_REGISTRATION` | Registro de inventario inicial. |
| `INVENTORY_ADJUSTMENT` | Ajuste de existencias. |
| `INVENTORY_STATUS_CHANGE` | Inventario marcado como dañado, bloqueado o habilitado. |
| `INVENTORY_RESTOCK` | Reintegro de unidades por devolución. |
| `ORDER_CONFIRMATION` | Confirmación de un pedido. |
| `ORDER_PAYMENT` | Pago de un pedido. |
| `ORDER_SHIPMENT` | Despacho de un pedido. |
| `ORDER_DELIVERY` | Entrega de un pedido. |
| `RETURN_REQUEST` | Solicitud de devolución. |
| `RETURN_APPROVAL` | Aprobación de una devolución. |
| `RETURN_REJECTION` | Rechazo de una devolución. |
| `REFUND_PROCESSING` | Procesamiento de un reembolso. |

---

# BusinessProcess

## Descripción

Procesos de negocio que controla `AuthorizationService`.

## Valores permitidos

| Valor | Significado |
|---|---|
| `SELLER_REGISTRATION` | Registro de vendedores: administrador. |
| `PRODUCT_REGISTRATION` | Registro de productos: vendedor. |
| `INVENTORY_MANAGEMENT` | Administración de inventario: vendedor y operador logístico. |
| `ORDER_MANAGEMENT` | Gestión de pedidos: comprador, vendedor y operador logístico. |
| `REFUND_MANAGEMENT` | Gestión de reembolsos: comprador y administrador. |
| `RETURN_MANAGEMENT` | Gestión de devoluciones: comprador y administrador (supuesto). |
| `WAREHOUSE_MANAGEMENT` | Administración de bodegas: administrador. |
| `SHIPPING_OPERATIONS` | Operación de despachos: operador logístico. |
| `USER_MANAGEMENT` | Administración de usuarios (bloquear y reactivar): administrador. |
| `ADMINISTRATIVE_REPORTS` | Consultas administrativas de seguimiento (OBJ-12): administrador y supervisor. |

---

# CommercialStatus

## Descripción

Condición del comprador para realizar compras.

## Valores permitidos

| Valor | Significado |
|---|---|
| `ENABLED` | Puede comprar. |
| `RESTRICTED` | No puede comprar. |

---

# InventoryStatus

## Descripción

Estado de un inventario.

## Valores permitidos

| Valor | Significado |
|---|---|
| `AVAILABLE` | Se puede reservar. |
| `DAMAGED` | No se puede reservar ni reintegrar. |
| `BLOCKED` | Detenido temporalmente. |

---

# MovementType

## Descripción

Tipo de movimiento de inventario (Dominio 6).

## Valores permitidos

| Valor | Significado |
|---|---|
| `INFLOW` | Ingreso. |
| `RESERVATION` | Reserva o liberación de reserva. |
| `SALE` | Salida por venta. |
| `ADJUSTMENT` | Ajuste. |
| `RETURN` | Devolución. |

---

# OrderStatus

## Descripción

Ciclo de estados del pedido (Dominio 7).

## Valores permitidos

| Valor | Significado |
|---|---|
| `CART` | Selección provisional; se representa con la entidad `Cart`. |
| `PENDING_PAYMENT` | Espera de confirmación financiera. |
| `PAID` | Inicio del alistamiento. |
| `SHIPPED` | Salida física de la bodega. |
| `DELIVERED` | Entregado y finalizado; ya no se modifica. |

---

# ProductStatus

## Descripción

Estado de publicación de un producto.

## Valores permitidos

| Valor | Significado |
|---|---|
| `PUBLISHED` | Visible en el catálogo. |
| `SUSPENDED` | No visible; puede volver a publicarse. |
| `DISCONTINUED` | Retirado de forma definitiva. |

---

# ProductType

## Descripción

Tipo de producto (Dominio 5).

## Valores permitidos

| Valor | Significado |
|---|---|
| `PHYSICAL` | Requiere inventario y despacho. |
| `DIGITAL` | Se entrega de inmediato tras el pago. |

---

# RefundStatus

## Descripción

Estado de un reembolso.

## Valores permitidos

| Valor | Significado |
|---|---|
| `PENDING` | Por procesar. |
| `PROCESSED` | Procesado. |

---

# ReservationStatus

## Descripción

Estado de una reserva de inventario.

## Valores permitidos

| Valor | Significado |
|---|---|
| `ACTIVE` | Unidades apartadas. |
| `CONFIRMED` | Convertida en salida por venta. |
| `RELEASED` | Unidades devueltas al disponible. |

---

# ReturnStatus

## Descripción

Estado de una devolución.

## Valores permitidos

| Valor | Significado |
|---|---|
| `REQUESTED` | Solicitada por el comprador. |
| `APPROVED` | Aprobada por el administrador. |
| `REJECTED` | Rechazada por el administrador. |

---

# ShipmentStatus

## Descripción

Estado de un envío.

## Valores permitidos

| Valor | Significado |
|---|---|
| `PENDING` | Creado, aún sin despachar. |
| `SHIPPED` | Despachado. |
| `DELIVERED` | Entregado. |

---

# UserRole

## Descripción

Rol único de un usuario (RG-02).

## Valores permitidos

| Valor | Significado |
|---|---|
| `BUYER` | Comprador. |
| `SELLER` | Vendedor. |
| `LOGISTIC_OPERATOR` | Operador logístico. |
| `ADMIN` | Administrador. |
| `SUPERVISOR` | Supervisor (consulta y seguimiento). |

---

# UserStatus

## Descripción

Condición operativa de un usuario.

## Valores permitidos

| Valor | Significado |
|---|---|
| `ACTIVE` | Puede operar. |
| `BLOCKED` | No puede ejecutar operaciones. |

---

# WarehouseOwnership

## Descripción

Clasificación de la bodega según su dueño (Dominio 4).

## Valores permitidos

| Valor | Significado |
|---|---|
| `MARKETPLACE` | Bodega del Marketplace. |
| `SELLER` | Bodega de un vendedor. |

---

# WarehouseType

## Descripción

Tipo de bodega.

## Valores permitidos

| Valor | Significado |
|---|---|
| `PHYSICAL` | Espacio físico, con dirección. |
| `DIGITAL` | Extensión del modelo, sin dirección física. |