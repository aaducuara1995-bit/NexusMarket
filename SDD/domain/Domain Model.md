# Domain Model

## Introducción

El modelo de dominio representa las entidades del negocio de NexusMarket, un marketplace que intermedia entre compradores y vendedores y administra el catálogo, el inventario distribuido por bodegas, los pedidos, la facturación, la logística y la posventa. Toma como base la especificación funcional del sistema.

Las entidades tienen identidad propia, cambian de estado con el tiempo y protegen sus propias reglas: un inventario nunca queda negativo, un pedido entregado ya no se modifica y un producto descontinuado no vuelve a publicarse. Las reglas que involucran a varias entidades a la vez se documentan en `Domain Services.md`.

El código está en inglés y esta documentación en español. Cada nombre de clase, atributo y método de este documento coincide exactamente con el código en `nexusmarket/src/main/java/application/domain/entities/`.

---

# Organización por área

```text
Usuarios: User, Buyer, Seller
Bodegas: Warehouse
Catálogo: Product
Inventario: Inventory, InventoryReservation, InventoryMovement
Compra: Cart, CartItem
Pedidos: Order, OrderItem
Facturación: Invoice
Logística: Shipment
Posventa: ReturnRequest, Refund
Auditoría: AuditLog
```

---

# Relaciones del dominio

```text
User ──(userId)──> Buyer
User ──(userId)──> Seller
Seller ──(sellerId)──> Product
Seller ──(sellerId)──> Warehouse (ownership = SELLER)
Product + Warehouse ──> Inventory
Inventory ──> InventoryReservation (por orderId)
Inventory ──> InventoryMovement
Buyer ──> Cart ──> CartItem ──> Product
Cart ──(al confirmarse)──> Order ──> OrderItem
Order ──> Invoice
Order ──> Shipment
Order ──> ReturnRequest
Order ──> Refund
User ──(performedByUserId)──> AuditLog ──(affectedEntityId)──> cualquier registro
```

---

# Entidades

---

# User

**Área:** Usuarios

## Descripción

Representa a una persona registrada en la plataforma. Es la base de la identificación y del control de acceso: cada operación del dominio recibe el `User` que la ejecuta.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del usuario. |
| `fullName` | `FullName` | Nombres y apellidos de la persona. |
| `email` | `Email` | Correo de acceso y comunicación. Único en la plataforma. |
| `document` | `IdentityDocument` | Documento de identidad. Único en la plataforma. |
| `role` | `UserRole` | Rol único del usuario dentro del sistema (RG-02). No cambia después de creado. |
| `status` | `UserStatus` | Estado operativo del usuario. Nace `ACTIVE`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static User restore(UUID id, FullName fullName, Email email, IdentityDocument document, UserRole role, UserStatus status)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void block()` | Bloquea al usuario. Falla si ya estaba bloqueado. |
| `void activate()` | Reactiva al usuario. Falla si ya estaba activo. |
| `boolean isActive()` | Indica si el usuario está activo. |
| `boolean hasRole(UserRole expected)` | Indica si el usuario tiene el rol indicado. |

## Relaciones

* Un `User` con rol `BUYER` se asocia a un `Buyer`, y uno con rol `SELLER` a un `Seller`, por medio de `userId`.

## Reglas de negocio

* Correo, documento, nombre y rol son obligatorios.
* El correo y el documento deben ser únicos en la plataforma (validación crítica de Usuarios); la unicidad la comprueba `RegisterUserService` por medio de `UserRepositoryPort`.
* Cada usuario tiene un único rol (RG-02).

---

# Buyer

**Área:** Usuarios

## Descripción

Perfil comercial de un usuario con rol de comprador. Guarda la información que necesita para comprar: sus direcciones de entrega y su estado comercial.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del comprador. |
| `userId` | `UUID` | Identificador del `User` al que pertenece este perfil. |
| `mainAddress` | `Address` | Dirección principal de entrega. Obligatoria. |
| `additionalAddresses` | `List<Address>` | Direcciones secundarias de entrega. Vacía por defecto. |
| `commercialStatus` | `CommercialStatus` | Condición del comprador para realizar compras. Nace `ENABLED`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Buyer restore(UUID id, UUID userId, Address mainAddress, List<Address> additionalAddresses, CommercialStatus commercialStatus)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void addAdditionalAddress(Address address)` | Agrega una dirección secundaria. Rechaza direcciones repetidas. |
| `void removeAdditionalAddress(Address address)` | Quita una dirección secundaria. |
| `void changeMainAddress(Address newAddress)` | Cambia la dirección principal; la anterior pasa a ser adicional. |
| `boolean hasAddress(Address address)` | Indica si una dirección es la principal o una de las adicionales. |
| `void restrict()` | Restringe al comprador para que no pueda comprar. |
| `void enable()` | Vuelve a habilitar al comprador. |
| `boolean canPurchase()` | Indica si el estado comercial permite comprar. |

## Relaciones

* Pertenece a un `User` por medio de `userId`.
* Es dueño de sus `Cart` y de sus `Order`.

## Reglas de negocio

* Un usuario tiene como máximo un perfil de comprador.
* El comprador nunca administra información de otros compradores ni inventarios (Dominio 2).

---

# Seller

**Área:** Usuarios

## Descripción

Perfil de un usuario con rol de vendedor. Identifica al dueño de los productos y de las bodegas de vendedor.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del vendedor. |
| `userId` | `UUID` | Identificador del `User` al que pertenece este perfil. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Seller restore(UUID id, UUID userId)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |

## Relaciones

* Pertenece a un `User` por medio de `userId`.
* Es dueño de `Product` y puede tener `Warehouse` propias (por `sellerId`).

## Reglas de negocio

* Los vendedores no se registran por sí mismos: los incorpora un administrador (Dominio 3).
* Un usuario tiene como máximo un perfil de vendedor.

---

# Warehouse

**Área:** Bodegas

## Descripción

Espacio de almacenamiento donde se guarda el inventario. Puede pertenecer al Marketplace o a un vendedor. Como extensión del modelo se contempla la bodega digital, sin dirección física.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único de la bodega. |
| `name` | `String` | Nombre de la bodega. Único en la plataforma. |
| `type` | `WarehouseType` | Tipo de bodega: física o digital. |
| `address` | `Address` | Dirección física. Obligatoria en bodegas físicas y vacía en las digitales. |
| `ownership` | `WarehouseOwnership` | Indica si la bodega es del Marketplace o de un vendedor (Dominio 4). |
| `sellerId` | `UUID` | Vendedor dueño de la bodega. Solo aplica cuando `ownership` es `SELLER`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Warehouse ofMarketplace(String name, WarehouseType type, Address address)` | Crea una bodega del Marketplace. |
| `static Warehouse ofSeller(UUID sellerId, String name, WarehouseType type, Address address)` | Crea una bodega de un vendedor; exige el `sellerId`. |
| `static Warehouse restore(UUID id, String name, WarehouseType type, Address address, WarehouseOwnership ownership, UUID sellerId)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `boolean isDigital()` | Indica si la bodega es digital. |
| `boolean isMarketplaceOwned()` | Indica si la bodega es del Marketplace. |
| `boolean belongsTo(UUID sellerIdToCheck)` | Indica si la bodega pertenece al vendedor indicado. |

## Relaciones

* Contiene registros de `Inventory`.
* Puede pertenecer a un `Seller`.

## Reglas de negocio

* Una bodega física debe tener dirección y una digital no la tiene.
* Una bodega de vendedor siempre tiene propietario; una del Marketplace no.

---

# Product

**Área:** Catálogo

## Descripción

Artículo físico o digital que un vendedor ofrece en el catálogo.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del producto. |
| `sellerId` | `UUID` | Vendedor dueño del producto. |
| `name` | `String` | Nombre del producto. No se repite dentro del mismo vendedor. |
| `description` | `String` | Descripción del producto. |
| `type` | `ProductType` | Físico o digital (Dominio 5). |
| `variants` | `List<String>` | Diferencias de color, talla, modelo, etc. Sin repetidos. |
| `price` | `Money` | Precio unitario. Siempre mayor que cero. |
| `status` | `ProductStatus` | Estado de publicación. Nace `SUSPENDED`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Product restore(UUID id, UUID sellerId, String name, String description, ProductType type, Money price, ProductStatus status, List<String> variants)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void addVariant(String variant)` | Agrega una variante. Rechaza vacías y repetidas. |
| `void changePrice(Money newPrice)` | Cambia el precio, que debe ser mayor que cero. |
| `void publish()` | Publica el producto. Un producto descontinuado no vuelve a publicarse. |
| `void suspend()` | Suspende un producto publicado. |
| `void discontinue()` | Descontinúa el producto de forma definitiva. |
| `boolean isPublished()` | Indica si el producto está publicado. |
| `boolean isPhysical()` | Indica si el producto es físico. |
| `boolean belongsTo(UUID sellerIdToCheck)` | Indica si el producto pertenece al vendedor indicado. |

## Relaciones

* Pertenece a un `Seller`.
* Sus existencias se registran en `Inventory`, una por bodega.

## Reglas de negocio

* Los productos físicos requieren inventario y despacho; los digitales se entregan de inmediato tras el pago.
* Un producto físico solo se publica si tiene existencias disponibles (lo valida `PublishProductService`).

---

# Inventory

**Área:** Inventario

## Descripción

Existencias de un producto en una bodega específica. El inventario es distribuido: un mismo producto puede tener existencias en varias bodegas.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del registro de inventario. |
| `product` | `Product` | Producto al que corresponden las existencias. |
| `warehouse` | `Warehouse` | Bodega donde están las existencias. |
| `quantity` | `int` | Unidades totales en la bodega. |
| `reserved` | `int` | Unidades apartadas para pedidos en curso. |
| `status` | `InventoryStatus` | Estado del inventario. Nace `AVAILABLE`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Inventory restore(UUID id, Product product, Warehouse warehouse, int quantity, int reserved, InventoryStatus status)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `int getAvailable()` | Devuelve las unidades disponibles: `quantity - reserved`. |
| `boolean isFor(UUID productId, UUID warehouseId)` | Indica si el registro corresponde al producto y la bodega indicados. |
| `void receive(int units)` | Movimiento de ingreso: suma unidades. |
| `void reserve(int units)` | Movimiento de reserva: aparta unidades disponibles. |
| `void releaseReservation(int units)` | Devuelve unidades reservadas al disponible. |
| `void confirmOutflow(int units)` | Salida por venta: descuenta unidades reservadas. |
| `void adjust(int difference)` | Ajuste positivo o negativo de las existencias. |
| `void restock(int units)` | Movimiento de devolución: reintegra unidades. |
| `void markDamaged()` | Marca el inventario como dañado. Exige que no tenga reservas. |
| `void block()` | Bloquea un inventario disponible. |
| `void enable()` | Vuelve a habilitar un inventario bloqueado. |

## Relaciones

* Pertenece a un `Product` y a una `Warehouse`.
* Sus reservas se registran en `InventoryReservation` y sus movimientos en `InventoryMovement`.

## Reglas de negocio

* No se permiten existencias negativas bajo ninguna circunstancia (Dominio 6).
* No se puede reservar inventario inexistente o marcado como dañado (validación crítica de Inventario).
* Un ajuste no puede dejar menos unidades que las reservadas.

---

# InventoryReservation

**Área:** Inventario

## Descripción

Registra las unidades de un inventario apartadas para un pedido. Permite saber qué deshacer si el pedido no se concreta o qué descontar cuando se despacha.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único de la reserva. |
| `orderId` | `UUID` | Pedido para el que se apartaron las unidades. |
| `inventory` | `Inventory` | Inventario del que se apartaron las unidades. |
| `quantity` | `int` | Unidades reservadas. |
| `date` | `LocalDateTime` | Fecha y hora de la reserva. |
| `status` | `ReservationStatus` | Estado de la reserva. Nace `ACTIVE`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static InventoryReservation create(UUID orderId, Inventory inventory, int quantity)` | Crea la reserva y aparta las unidades en el inventario en el mismo acto. |
| `static InventoryReservation restore(UUID id, UUID orderId, Inventory inventory, int quantity, LocalDateTime date, ReservationStatus status)` | Reconstruye una reserva guardada sin volver a apartar unidades: el inventario guardado ya las cuenta. |
| `void release()` | Libera la reserva y devuelve las unidades al disponible. |
| `void confirmOutflow()` | Confirma la salida por venta de las unidades reservadas. |
| `boolean isActive()` | Indica si la reserva sigue activa. |
| `boolean belongsToOrder(UUID orderIdToCheck)` | Indica si la reserva pertenece al pedido indicado. |

## Relaciones

* Pertenece a un `Inventory` y a un `Order` (por `orderId`).

## Reglas de negocio

* Una reserva se cierra una sola vez: se libera o se confirma, nunca ambas.

---

# InventoryMovement

**Área:** Inventario

## Descripción

Registro histórico de cada movimiento de inventario. Da la trazabilidad que pide la especificación.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del movimiento. |
| `inventoryId` | `UUID` | Inventario afectado. |
| `productId` | `UUID` | Producto afectado. |
| `warehouseId` | `UUID` | Bodega afectada. |
| `type` | `MovementType` | Tipo de movimiento: ingreso, reserva, salida por venta, ajuste o devolución. |
| `quantity` | `int` | Unidades del movimiento. Negativa en salidas y liberaciones. |
| `reason` | `String` | Motivo del movimiento. Obligatorio. |
| `date` | `LocalDateTime` | Fecha y hora del movimiento. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static InventoryMovement restore(UUID id, UUID inventoryId, UUID productId, UUID warehouseId, MovementType type, int quantity, String reason, LocalDateTime date)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |

## Relaciones

* Se refiere a un `Inventory`, un `Product` y una `Warehouse`.

## Reglas de negocio

* Es inmutable: una vez creado no se modifica.
* La cantidad no puede ser cero.

---

# Cart

**Área:** Compra

## Descripción

Selección provisional de productos de un comprador. Representa la etapa "Carrito" del ciclo de vida del pedido.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del carrito. |
| `buyer` | `Buyer` | Comprador dueño del carrito. |
| `items` | `List<CartItem>` | Productos seleccionados con su cantidad. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Cart restore(UUID id, Buyer buyer, List<CartItem> items)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void addProduct(Product product, int quantity)` | Agrega un producto publicado; si ya estaba, suma la cantidad. |
| `void removeProduct(UUID productId)` | Quita un producto del carrito. |
| `void clear()` | Vacía el carrito. |

## Relaciones

* Pertenece a un `Buyer`.
* Contiene `CartItem`.
* Al confirmarse da origen a un `Order`.

## Reglas de negocio

* Solo admite productos publicados.
* Agregar al carrito no reserva stock.

---

# CartItem

**Área:** Compra

## Descripción

Línea de un carrito: un producto y la cantidad que el comprador quiere.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `product` | `Product` | Producto seleccionado. |
| `quantity` | `int` | Unidades seleccionadas. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static CartItem restore(Product product, int quantity)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |

## Relaciones

* Pertenece a un `Cart`.

## Reglas de negocio

* Solo el `Cart` puede crear o modificar sus líneas.

---

# Order

**Área:** Pedidos

## Descripción

Compromiso comercial formal de un comprador. Su ciclo de vida es el proceso central del sistema (Dominio 7).

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del pedido. |
| `buyer` | `Buyer` | Comprador que realizó el pedido. |
| `items` | `List<OrderItem>` | Líneas del pedido, copiadas del carrito al confirmarlo. |
| `date` | `LocalDateTime` | Fecha y hora de creación. |
| `deliveryAddress` | `Address` | Dirección de entrega que eligió el comprador al confirmar. Si no elige, se usa su dirección principal. |
| `status` | `OrderStatus` | Estado del pedido. Nace `PENDING_PAYMENT`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Order restore(UUID id, Buyer buyer, List<OrderItem> items, LocalDateTime date, Address deliveryAddress, OrderStatus status)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `Money calculateTotal()` | Suma los subtotales de todas las líneas. |
| `boolean containsPhysicalProducts()` | Indica si el pedido tiene algún producto físico. |
| `boolean isDigitalOnly()` | Indica si el pedido tiene solo productos digitales. |
| `boolean belongsTo(UUID buyerId)` | Indica si el pedido pertenece al comprador indicado. |
| `void pay()` | Pasa de `PENDING_PAYMENT` a `PAID`. |
| `void ship()` | Pasa de `PAID` a `SHIPPED`. |
| `void complete()` | Pasa de `SHIPPED` a `DELIVERED`. |

## Relaciones

* Pertenece a un `Buyer`.
* Contiene `OrderItem`.
* Puede tener una `Invoice`, un `Shipment`, una `ReturnRequest` y un `Refund`.

## Reglas de negocio

* Los estados avanzan en orden: `PENDING_PAYMENT`, `PAID`, `SHIPPED`, `DELIVERED`.
* Un pedido finalizado no puede modificarse bajo ninguna circunstancia (validación crítica de Pedidos).
* La etapa `CART` del ciclo se representa con la entidad `Cart`.
* La dirección de entrega debe ser una de las direcciones registradas del comprador.

---

# OrderItem

**Área:** Pedidos

## Descripción

Línea de un pedido. Guarda una copia de los datos del producto al momento de la compra, para que un cambio posterior de precio o nombre no altere el pedido.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `productId` | `UUID` | Producto comprado. |
| `productName` | `String` | Nombre del producto al momento de la compra. |
| `type` | `ProductType` | Tipo del producto al momento de la compra. |
| `quantity` | `int` | Unidades compradas. |
| `unitPrice` | `Money` | Precio unitario al momento de la compra. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static OrderItem restore(UUID productId, String productName, ProductType type, int quantity, Money unitPrice)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `Money subtotal()` | Devuelve `unitPrice * quantity`. |
| `boolean isPhysical()` | Indica si el producto comprado es físico. |

## Relaciones

* Pertenece a un `Order`.

## Reglas de negocio

* Es inmutable.

---

# Invoice

**Área:** Facturación

## Descripción

Información comercial de la venta de un pedido pagado.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único de la factura. |
| `order` | `Order` | Pedido facturado. |
| `total` | `Money` | Total facturado, tomado del pedido. |
| `date` | `LocalDateTime` | Fecha y hora de emisión. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Invoice restore(UUID id, Order order, Money total, LocalDateTime date)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |

## Relaciones

* Pertenece a un `Order`.

## Reglas de negocio

* Solo se factura un pedido pagado.
* El total sale del pedido y no se modifica después de emitida.
* Un pedido tiene como máximo una factura.

---

# Shipment

**Área:** Logística

## Descripción

Proceso logístico de entrega de un pedido con productos físicos.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del envío. |
| `order` | `Order` | Pedido que se envía. |
| `deliveryAddress` | `Address` | Dirección de entrega, que debe ser del comprador. |
| `status` | `ShipmentStatus` | Estado del envío. Nace `PENDING`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Shipment restore(UUID id, Order order, Address deliveryAddress, ShipmentStatus status)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void ship()` | Pasa de `PENDING` a `SHIPPED`. |
| `void deliver()` | Pasa de `SHIPPED` a `DELIVERED`. |

## Relaciones

* Pertenece a un `Order`.

## Reglas de negocio

* Solo se crea para un pedido pagado con productos físicos.
* Su estado avanza junto con el del pedido (lo coordinan `ShipOrderService` y `ConfirmDeliveryService`).

---

# ReturnRequest

**Área:** Posventa

## Descripción

Solicitud de un comprador para devolver un pedido entregado.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único de la devolución. |
| `order` | `Order` | Pedido devuelto. |
| `reason` | `String` | Motivo de la devolución. Obligatorio. |
| `status` | `ReturnStatus` | Estado de la devolución. Nace `REQUESTED`. |
| `restockedUnits` | `Map<UUID, Integer>` | Unidades ya reintegradas al inventario, por producto. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static ReturnRequest restore(UUID id, Order order, String reason, ReturnStatus status, Map<UUID, Integer> restockedUnits)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void approve()` | Aprueba la devolución. |
| `void reject()` | Rechaza la devolución. |
| `boolean isApproved()` | Indica si la devolución fue aprobada. |
| `boolean isOpen()` | Indica si la devolución está solicitada o aprobada. |
| `int purchasedUnits(UUID productId)` | Unidades compradas de un producto en el pedido. |
| `int restockedUnits(UUID productId)` | Unidades de un producto ya reintegradas. |
| `int unitsPendingRestock(UUID productId)` | Unidades de un producto que aún pueden reintegrarse. |
| `void registerRestock(UUID productId, int units)` | Registra unidades reintegradas, sin superar las compradas. |

## Relaciones

* Pertenece a un `Order`.

## Reglas de negocio

* Solo se devuelve un pedido entregado.
* Se resuelve una sola vez (aprobada o rechazada).
* Las unidades de una devolución se reintegran una sola vez.

---

# Refund

**Área:** Posventa

## Descripción

Devolución del dinero de un pedido pagado.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del reembolso. |
| `order` | `Order` | Pedido reembolsado. |
| `amount` | `Money` | Monto a reembolsar. |
| `status` | `RefundStatus` | Estado del reembolso. Nace `PENDING`. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static Refund restore(UUID id, Order order, Money amount, RefundStatus status)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |
| `void process()` | Marca el reembolso como procesado. |

## Relaciones

* Pertenece a un `Order`.

## Reglas de negocio

* El monto es mayor que cero, en la moneda del pedido y no supera su total.
* Se procesa una sola vez.

---

# AuditLog

**Área:** Auditoría

## Descripción

Registro inmutable de una operación del negocio: quién la hizo, con qué rol, cuándo y sobre qué registro. Se guarda en MongoDB, igual que la auditoría del proyecto de referencia, y da la trazabilidad que pide la especificación.

## Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` | Identificador único del registro. |
| `operation` | `AuditOperation` | Operación auditada. |
| `date` | `LocalDateTime` | Fecha y hora de la operación. |
| `performedByUserId` | `UUID` | Usuario que hizo la operación. |
| `performedByRole` | `UserRole` | Rol del usuario en ese momento. |
| `affectedEntityId` | `UUID` | Registro afectado (pedido, producto, inventario, usuario, etc.). |
| `details` | `Map<String, String>` | Datos adicionales de la operación, como el nuevo estado o el monto. |

## Comportamiento

| Método | Descripción |
|---|---|
| `static AuditLog restore(UUID id, AuditOperation operation, LocalDateTime date, UUID performedByUserId, UserRole performedByRole, UUID affectedEntityId, Map<String, String> details)` | Reconstruye un objeto guardado por un adaptador de persistencia, con su id y su estado. No sirve para crear objetos nuevos. |

## Relaciones

* Referencia a un `User` por su id.
* Referencia al registro afectado por su id, sin depender de su tipo.

## Reglas de negocio

* Es inmutable: el registro solo crece, nunca se modifica ni se borra.
* Exige la operación, el usuario y el registro afectado.
* Guarda una copia de los detalles, así que cambiarlos después no altera el registro.

---

# Decisiones de diseño y supuestos

La especificación funcional no define algunos casos. Se resolvieron así:

* **Bodega digital.** La especificación solo habla de bodegas físicas. La bodega digital, sin dirección, es una extensión del modelo pensada para los productos digitales.
* **Productos digitales.** No usan inventario ni envío, porque se entregan de inmediato tras el pago.
* **Pedidos mixtos.** Un pedido con productos físicos y digitales sigue el flujo de logística completo, porque el estado del pedido es uno solo.
* **Devoluciones.** Las solicita el comprador y las resuelve el administrador. Cubren el pedido completo, así que el reembolso es por el total.
* **Gestión de devoluciones.** La matriz de responsabilidades no la incluye; se asignó a comprador y administrador, igual que los reembolsos.
* **Unicidad de nombres.** El nombre de una bodega es único en la plataforma; el de un producto es único dentro de cada vendedor.
* **Perfiles.** `Buyer` y `Seller` se relacionan con `User` por `userId` en lugar de heredar de él, porque cada usuario tiene un único rol (RG-02) y su perfil guarda datos propios del negocio.
* **Reconstrucción desde la base de datos.** Los constructores públicos crean objetos nuevos: generan el id y fijan el estado inicial. Para leer de MySQL cada entidad tiene un método estático `restore(...)` que recibe el id y el estado guardados. Solo lo usan los adaptadores de persistencia, y no vuelve a ejecutar efectos de creación (por ejemplo, restaurar una reserva no aparta unidades otra vez).