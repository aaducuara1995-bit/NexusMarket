# API REST Endpoints

## Introducción

Estos son todos los endpoints de NexusMarket. Cada controlador atiende un solo rol y llama solo a su puerto de entrada, igual que en el proyecto de referencia. La tabla se genera desde el código de `adapters/rest/controllers/`.

Hay 46 endpoints en 6 controladores.

* **Base:** `http://localhost:8080`
* **Usuario que hace la petición (Fase C):** encabezado `X-User-Email` con el correo de un usuario registrado. Al arrancar existe `admin@nexusmarket.com`. En la Fase D este encabezado se reemplaza por el token JWT (`Authorization: Bearer ...`).
* **Formato:** JSON. Los ids son UUID. Los montos van en pesos colombianos (COP) si no se indica otra moneda.
* **Errores:** siempre con el mismo cuerpo, ver `Global-exception-handler.md`.

---

## PublicAccessRestController

**Ruta base:** `/api/v1/public`  
**Rol:** Público, sin encabezado  
**Puerto de entrada:** `PublicAccessPort`

| Método | Ruta | Cuerpo | Respuesta | Qué hace |
|---|---|---|---|---|
| `POST` | `/api/v1/public/buyers` | `RegisterBuyerRequestDTO` | 201 `BuyerResponseDTO` | Registra un comprador nuevo (usuario con rol BUYER y su dirección principal). |
| `GET` | `/api/v1/public/products` | — | 200 `lista de ProductResponseDTO` | Lista los productos publicados. |
| `GET` | `/api/v1/public/products/{productId}/availability` | — | 200 `AvailabilityResponseDTO` | Unidades disponibles de un producto, sumando todas las bodegas. |

## BuyerRestController

**Ruta base:** `/api/v1/buyer`  
**Rol:** `BUYER`  
**Puerto de entrada:** `BuyerPort`

| Método | Ruta | Cuerpo | Respuesta | Qué hace |
|---|---|---|---|---|
| `GET` | `/api/v1/buyer/cart` | — | 200 `CartResponseDTO` | Muestra el carrito del comprador. |
| `POST` | `/api/v1/buyer/cart/items` | `AddToCartRequestDTO` | 200 `CartResponseDTO` | Agrega unidades de un producto al carrito. |
| `DELETE` | `/api/v1/buyer/cart/items/{productId}` | — | 200 `CartResponseDTO` | Quita un producto del carrito. |
| `POST` | `/api/v1/buyer/orders` | `ConfirmOrderRequestDTO` (opcional) | 201 `OrderResponseDTO` | Convierte el carrito en pedido y reserva el stock. Cuerpo opcional con deliveryAddress. |
| `POST` | `/api/v1/buyer/orders/{orderId}/payment` | `PaymentRequestDTO` | 200 `OrderResponseDTO` | Paga el pedido: emite la factura y entrega lo digital o crea el envío. |
| `GET` | `/api/v1/buyer/orders` | — | 200 `lista de OrderResponseDTO` | Lista los pedidos del comprador. |
| `POST` | `/api/v1/buyer/orders/{orderId}/returns` | `ReturnRequestDTO` | 201 `ReturnRequestResponseDTO` | Solicita la devolución de un pedido entregado. |

## SellerRestController

**Ruta base:** `/api/v1/seller`  
**Rol:** `SELLER`  
**Puerto de entrada:** `SellerPort`

| Método | Ruta | Cuerpo | Respuesta | Qué hace |
|---|---|---|---|---|
| `POST` | `/api/v1/seller/products` | `ProductRequestDTO` | 201 `ProductResponseDTO` | Registra un producto del vendedor (nace suspendido). |
| `GET` | `/api/v1/seller/products` | — | 200 `lista de ProductResponseDTO` | Lista los productos del vendedor. |
| `PATCH` | `/api/v1/seller/products/{productId}/publish` | — | 200 `ProductResponseDTO` | Publica un producto. Uno físico necesita stock disponible. |
| `PATCH` | `/api/v1/seller/products/{productId}/suspend` | — | 200 `ProductResponseDTO` | Suspende un producto. |
| `PATCH` | `/api/v1/seller/products/{productId}/discontinue` | — | 200 `ProductResponseDTO` | Descontinúa un producto sin reservas activas. |
| `POST` | `/api/v1/seller/inventories` | `InitialInventoryRequestDTO` | 201 `InventoryResponseDTO` | Registra el stock inicial de un producto físico en una bodega. |
| `PATCH` | `/api/v1/seller/inventories/{inventoryId}/adjustment` | `AdjustInventoryRequestDTO` | 200 `InventoryResponseDTO` | Ajusta existencias con un motivo (difference positiva o negativa). |
| `PATCH` | `/api/v1/seller/inventories/{inventoryId}/damaged` | — | 200 `InventoryResponseDTO` | Marca un inventario como dañado. |
| `PATCH` | `/api/v1/seller/inventories/{inventoryId}/block` | — | 200 `InventoryResponseDTO` | Bloquea un inventario. |
| `PATCH` | `/api/v1/seller/inventories/{inventoryId}/enable` | — | 200 `InventoryResponseDTO` | Habilita un inventario bloqueado. |
| `PATCH` | `/api/v1/seller/inventories/{inventoryId}/restock` | `RestockRequestDTO` | 200 `InventoryResponseDTO` | Reintegra al stock unidades de una devolución aprobada. |

## LogisticOperatorRestController

**Ruta base:** `/api/v1/logistics`  
**Rol:** `LOGISTIC_OPERATOR`  
**Puerto de entrada:** `LogisticOperatorPort`

| Método | Ruta | Cuerpo | Respuesta | Qué hace |
|---|---|---|---|---|
| `GET` | `/api/v1/logistics/orders/to-ship` | — | 200 `lista de OrderResponseDTO` | Pedidos pagados con productos físicos, pendientes de despacho. |
| `PATCH` | `/api/v1/logistics/orders/{orderId}/ship` | — | 200 `OrderResponseDTO` | Despacha un pedido: salida definitiva del inventario. |
| `PATCH` | `/api/v1/logistics/orders/{orderId}/deliver` | — | 200 `OrderResponseDTO` | Confirma la entrega de un pedido despachado. |
| `POST` | `/api/v1/logistics/inventories` | `InitialInventoryRequestDTO` | 201 `InventoryResponseDTO` | Registra el stock inicial de un producto físico en una bodega. |
| `PATCH` | `/api/v1/logistics/inventories/{inventoryId}/adjustment` | `AdjustInventoryRequestDTO` | 200 `InventoryResponseDTO` | Ajusta existencias con un motivo (difference positiva o negativa). |
| `PATCH` | `/api/v1/logistics/inventories/{inventoryId}/damaged` | — | 200 `InventoryResponseDTO` | Marca un inventario como dañado. |
| `PATCH` | `/api/v1/logistics/inventories/{inventoryId}/block` | — | 200 `InventoryResponseDTO` | Bloquea un inventario. |
| `PATCH` | `/api/v1/logistics/inventories/{inventoryId}/enable` | — | 200 `InventoryResponseDTO` | Habilita un inventario bloqueado. |
| `PATCH` | `/api/v1/logistics/inventories/{inventoryId}/restock` | `RestockRequestDTO` | 200 `InventoryResponseDTO` | Reintegra al stock unidades de una devolución aprobada. |

## AdminRestController

**Ruta base:** `/api/v1/admin`  
**Rol:** `ADMIN`  
**Puerto de entrada:** `AdminPort`

| Método | Ruta | Cuerpo | Respuesta | Qué hace |
|---|---|---|---|---|
| `POST` | `/api/v1/admin/users` | `RegisterUserRequestDTO` | 201 `UserResponseDTO` | Registra un usuario con cualquier rol. |
| `PATCH` | `/api/v1/admin/users/{userId}/block` | — | 200 `UserResponseDTO` | Bloquea un usuario. Un administrador no se puede bloquear a sí mismo. |
| `PATCH` | `/api/v1/admin/users/{userId}/activate` | — | 200 `UserResponseDTO` | Reactiva un usuario bloqueado. |
| `POST` | `/api/v1/admin/sellers` | `OnboardSellerRequestDTO` | 201 `SellerResponseDTO` | Da de alta un vendedor con su primera bodega (RN-01). |
| `POST` | `/api/v1/admin/warehouses` | `WarehouseRequestDTO` | 201 `WarehouseResponseDTO` | Registra una bodega del marketplace. |
| `POST` | `/api/v1/admin/sellers/{sellerId}/warehouses` | `WarehouseRequestDTO` | 201 `WarehouseResponseDTO` | Registra otra bodega para un vendedor. |
| `PATCH` | `/api/v1/admin/returns/{returnId}/approve` | — | 200 `ReturnRequestResponseDTO` | Aprueba una devolución y genera su reembolso (devuelve refundId). |
| `PATCH` | `/api/v1/admin/returns/{returnId}/reject` | — | 200 `ReturnRequestResponseDTO` | Rechaza una devolución. |
| `PATCH` | `/api/v1/admin/refunds/{refundId}/process` | — | 200 `RefundResponseDTO` | Procesa un reembolso pendiente. |
| `GET` | `/api/v1/admin/audit-logs` | — | 200 `lista de AuditLogResponseDTO` | Auditoría completa, del registro más reciente al más antiguo. |

## SupervisorRestController

**Ruta base:** `/api/v1/supervisor`  
**Rol:** `SUPERVISOR` (y `ADMIN` en la auditoría)  
**Puerto de entrada:** `SupervisorPort`

| Método | Ruta | Cuerpo | Respuesta | Qué hace |
|---|---|---|---|---|
| `GET` | `/api/v1/supervisor/users` | — | 200 `lista de UserResponseDTO` | Lista los usuarios. |
| `GET` | `/api/v1/supervisor/warehouses` | — | 200 `lista de WarehouseResponseDTO` | Lista las bodegas. |
| `GET` | `/api/v1/supervisor/inventories` | — | 200 `lista de InventoryResponseDTO` | Lista el inventario de todas las bodegas. |
| `GET` | `/api/v1/supervisor/orders` | — | 200 `lista de OrderResponseDTO` | Lista todos los pedidos. |
| `GET` | `/api/v1/supervisor/audit-logs` | — | 200 `lista de AuditLogResponseDTO` | Auditoría completa, del registro más reciente al más antiguo. |
| `GET` | `/api/v1/supervisor/audit-logs/{entityId}` | — | 200 `lista de AuditLogResponseDTO` | Auditoría de un registro: pedido, producto, usuario, inventario... |

---

## Cuerpos de las peticiones

Los campos con anotaciones de `jakarta.validation` se revisan antes de llegar al caso de uso (`@Valid`). Si algo no cumple, la respuesta es 400 con el campo y el problema.

### AddToCartRequestDTO

| Campo | Tipo | Validación |
|---|---|---|
| `productId` | `UUID` | `@NotNull` |
| `quantity` | `Integer` | `@NotNull`, `@Positive` |

### AdjustInventoryRequestDTO

| Campo | Tipo | Validación |
|---|---|---|
| `difference` | `Integer` | `@NotNull` |
| `reason` | `String` | `@NotBlank` |

### ConfirmOrderRequestDTO

| Campo | Tipo | Validación |
|---|---|---|
| `deliveryAddress` | `AddressDTO` | `@Valid` |

### InitialInventoryRequestDTO

| Campo | Tipo | Validación |
|---|---|---|
| `productId` | `UUID` | `@NotNull` |
| `warehouseId` | `UUID` | `@NotNull` |
| `quantity` | `Integer` | `@NotNull`, `@PositiveOrZero` |

### OnboardSellerRequestDTO

| Campo | Tipo | Validación |
|---|---|---|
| `userId` | `UUID` | `@NotNull` |
| `firstWarehouse` | `WarehouseRequestDTO` | `@NotNull`, `@Valid` |

### PaymentRequestDTO

| Campo | Tipo | Validación |
|---|---|---|
| `amount` | `BigDecimal` | `@NotNull`, `@Positive` |
| `currency` | `String` | opcional |

### ProductRequestDTO

| Campo | Tipo | Validación |
|---|---|---|
| `name` | `String` | `@NotBlank` |
| `description` | `String` | opcional |
| `type` | `ProductType` | `@NotNull` |
| `price` | `BigDecimal` | `@NotNull`, `@Positive` |
| `currency` | `String` | opcional |
| `variants` | `List<String>` | opcional |

### RegisterBuyerRequestDTO

| Campo | Tipo | Validación |
|---|---|---|
| `firstNames` | `String` | `@NotBlank` |
| `lastNames` | `String` | `@NotBlank` |
| `email` | `String` | `@NotBlank`, `@Email` |
| `document` | `String` | `@NotBlank`, `@Pattern(regexp = "\\d+", message = "must contain only digits")` |
| `mainAddress` | `AddressDTO` | `@NotNull`, `@Valid` |

### RegisterUserRequestDTO

| Campo | Tipo | Validación |
|---|---|---|
| `firstNames` | `String` | `@NotBlank` |
| `lastNames` | `String` | `@NotBlank` |
| `email` | `String` | `@NotBlank`, `@Email` |
| `document` | `String` | `@NotBlank`, `@Pattern(regexp = "\\d+", message = "must contain only digits")` |
| `role` | `UserRole` | `@NotNull` |

### RestockRequestDTO

| Campo | Tipo | Validación |
|---|---|---|
| `returnId` | `UUID` | `@NotNull` |
| `units` | `Integer` | `@NotNull`, `@Positive` |

### ReturnRequestDTO

| Campo | Tipo | Validación |
|---|---|---|
| `reason` | `String` | `@NotBlank` |

### WarehouseRequestDTO

| Campo | Tipo | Validación |
|---|---|---|
| `name` | `String` | `@NotBlank` |
| `type` | `WarehouseType` | `@NotNull` |
| `address` | `AddressDTO` | `@Valid` |

### AddressDTO

| Campo | Tipo | Validación |
|---|---|---|
| `street` | `String` | `@NotBlank` |
| `city` | `String` | `@NotBlank` |
| `department` | `String` | `@NotBlank` |
| `postalCode` | `String` | `@NotBlank` |

---

## Ejemplo: una compra completa

```text
1. POST  /api/v1/admin/users                 (X-User-Email: admin)   crea el usuario SELLER
2. POST  /api/v1/admin/sellers               (admin)                 alta del vendedor + bodega
3. POST  /api/v1/seller/products             (vendedor)              registra el producto
4. POST  /api/v1/seller/inventories          (vendedor)              stock inicial en su bodega
5. PATCH /api/v1/seller/products/{id}/publish (vendedor)             lo publica
6. POST  /api/v1/public/buyers               (sin encabezado)        se registra el comprador
7. POST  /api/v1/buyer/cart/items            (comprador)             agrega al carrito
8. POST  /api/v1/buyer/orders                (comprador)             confirma: pedido PENDING_PAYMENT
9. POST  /api/v1/buyer/orders/{id}/payment   (comprador)             paga: factura + envío
10. PATCH /api/v1/logistics/orders/{id}/ship    (operador)            despacha
11. PATCH /api/v1/logistics/orders/{id}/deliver (operador)            entrega
```