# Persistence Adapters

## 1. Descripción general

Los adaptadores de persistencia están en `adapters/persistence/` e implementan los puertos de salida de `domain/ports/out/`. Siguen las reglas del proyecto de referencia:

1. Las entidades del dominio nunca se usan como tablas. Cada adaptador tiene sus propias **entidades de persistencia** (los DTO del repositorio), sus **mappers** (dominio ↔ entidad) y sus **repositorios de Spring Data**.
2. **Spring Data JPA con MySQL** guarda los datos del negocio (14 puertos, sección 6).
3. **Spring Data MongoDB** guarda la auditoría (`AuditLogRepositoryPort`, sección 7), igual que en el ejemplo del profesor.

---

## 2. Flujo

```text
Use Case Adapter / Domain Service
      |
      v  llama al puerto
Output Port (domain/ports/out)
      ^
      |  implementado por
XxxJpaAdapter (adapters/persistence/jpa)
      |  1. mapper: dominio -> XxxJpaEntity
      |  2. SpringDataJpaXxxRepository.save / findBy...
      |  3. mapper: XxxJpaEntity -> dominio (con restore)
      v
MySQL
```

---

## 3. Estructura

```text
adapters/persistence/
├── jpa/
│   ├── entities/        14 entidades JPA y 3 @Embeddable (dirección, línea de carrito, línea de pedido)
│   ├── mappers/         un mapper por agregado y CommonJpaMapper (ids, direcciones, dinero)
│   ├── repositories/    14 interfaces SpringDataJpaXxxRepository
│   ├── JpaReferenceResolver.java   reconstruye los objetos referenciados por id
│   └── XxxJpaAdapter.java          14 adaptadores, uno por puerto
└── mongodb/
    ├── documents/       AuditLogDocument (colección audit_logs)
    ├── mappers/         AuditLogMongoMapper
    ├── repositories/    AuditLogMongoRepository
    └── AuditLogMongoAdapter.java   implementa AuditLogRepositoryPort
```

---

## 4. Decisiones de diseño

* **Ids como texto.** Los `UUID` del dominio se guardan como `VARCHAR(36)`, legibles desde MySQL Workbench.
* **Enums como texto.** Todos los enums se guardan con `@Enumerated(EnumType.STRING)`: la tabla muestra `PAID` y no un número.
* **Referencias por id.** Una tabla nunca guarda otro agregado completo: el inventario guarda `product_id` y `warehouse_id`, la factura `order_id`. Al leer, `JpaReferenceResolver` reconstruye el objeto del dominio que hace falta (el producto, la bodega, el pedido con su comprador). Así no hay relaciones `@ManyToOne` que carguen medio modelo ni cascadas que borren de más.
* **Colecciones propias del agregado.** Las direcciones adicionales del comprador, las variantes del producto, las líneas del carrito y del pedido, y las unidades reintegradas de una devolución van en tablas hijas con `@ElementCollection`. Se cargan junto con su dueño (`EAGER`), porque el mapper las necesita para reconstruir la entidad.
* **Reconstrucción con `restore`.** Los mappers leen con los métodos `restore(...)` del dominio, que respetan el id y el estado guardados.
* **Una reserva, un inventario.** Al leer las reservas de un pedido, las que apuntan al mismo inventario comparten un solo objeto `Inventory`, para que sus cambios se sumen y no se pisen al guardar.
* **Dinero.** `Money` se guarda en dos columnas: monto `DECIMAL(15,2)` y moneda de tres letras.

---

## 5. Adaptadores

| Adaptador | Puerto que implementa | Repositorio Spring Data | Usa el resolvedor |
|---|---|---|---|
| `BuyerJpaAdapter` | `BuyerRepositoryPort` | `SpringDataJpaBuyerRepository` | no |
| `CartJpaAdapter` | `CartRepositoryPort` | `SpringDataJpaCartRepository` | sí |
| `InventoryJpaAdapter` | `InventoryRepositoryPort` | `SpringDataJpaInventoryRepository` | sí |
| `InventoryMovementJpaAdapter` | `InventoryMovementRepositoryPort` | `SpringDataJpaInventoryMovementRepository` | no |
| `InventoryReservationJpaAdapter` | `InventoryReservationRepositoryPort` | `SpringDataJpaInventoryReservationRepository` | sí |
| `InvoiceJpaAdapter` | `InvoiceRepositoryPort` | `SpringDataJpaInvoiceRepository` | sí |
| `OrderJpaAdapter` | `OrderRepositoryPort` | `SpringDataJpaOrderRepository` | sí |
| `ProductJpaAdapter` | `ProductRepositoryPort` | `SpringDataJpaProductRepository` | no |
| `RefundJpaAdapter` | `RefundRepositoryPort` | `SpringDataJpaRefundRepository` | sí |
| `ReturnRequestJpaAdapter` | `ReturnRequestRepositoryPort` | `SpringDataJpaReturnRequestRepository` | sí |
| `SellerJpaAdapter` | `SellerRepositoryPort` | `SpringDataJpaSellerRepository` | no |
| `ShipmentJpaAdapter` | `ShipmentRepositoryPort` | `SpringDataJpaShipmentRepository` | sí |
| `UserJpaAdapter` | `UserRepositoryPort` | `SpringDataJpaUserRepository` | no |
| `WarehouseJpaAdapter` | `WarehouseRepositoryPort` | `SpringDataJpaWarehouseRepository` | no |

---

## 6. Tablas de MySQL

Hibernate crea y actualiza las tablas solo (`ddl-auto=update`, se configura en la Fase B3).

### `buyers` (BuyerJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `user_id` | `String` | userId (único) |
| `street` | `String` | street (dirección) |
| `city` | `String` | city (dirección) |
| `department` | `String` | department (dirección) |
| `postal_code` | `String` | postalCode (dirección) |
| — | `List<AddressEmbeddable>` | additionalAddresses: tabla `buyer_addresses` |
| `commercial_status` | `CommercialStatus` | commercialStatus (enum como texto) |

Tabla hija `buyer_addresses`, unida por `buyer_id`.

### `carts` (CartJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `buyer_id` | `String` | buyerId (único) |
| — | `List<CartItemEmbeddable>` | items: tabla `cart_items` |

Tabla hija `cart_items`, unida por `cart_id`.

### `inventories` (InventoryJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `product_id` | `String` | productId |
| `warehouse_id` | `String` | warehouseId |
| `quantity` | `int` | quantity |
| `reserved` | `int` | reserved |
| `status` | `InventoryStatus` | status (enum como texto) |

### `inventory_movements` (InventoryMovementJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `inventory_id` | `String` | inventoryId |
| `product_id` | `String` | productId |
| `warehouse_id` | `String` | warehouseId |
| `type` | `MovementType` | type (enum como texto) |
| `quantity` | `int` | quantity |
| `reason` | `String` | reason |
| `moved_at` | `LocalDateTime` | date |

### `inventory_reservations` (InventoryReservationJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `order_id` | `String` | orderId |
| `inventory_id` | `String` | inventoryId |
| `quantity` | `int` | quantity |
| `reserved_at` | `LocalDateTime` | date |
| `status` | `ReservationStatus` | status (enum como texto) |

### `invoices` (InvoiceJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `order_id` | `String` | orderId (único) |
| `total_amount` | `BigDecimal` | totalAmount |
| `total_currency` | `String` | totalCurrency |
| `issued_at` | `LocalDateTime` | date |

### `orders` (OrderJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `buyer_id` | `String` | buyerId |
| `created_at` | `LocalDateTime` | date |
| `street` | `String` | street (dirección) |
| `city` | `String` | city (dirección) |
| `department` | `String` | department (dirección) |
| `postal_code` | `String` | postalCode (dirección) |
| `status` | `OrderStatus` | status (enum como texto) |
| — | `List<OrderItemEmbeddable>` | items: tabla `order_items` |

Tabla hija `order_items`, unida por `order_id`.

### `products` (ProductJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `seller_id` | `String` | sellerId |
| `name` | `String` | name |
| `description` | `String` | description |
| `type` | `ProductType` | type (enum como texto) |
| `price_amount` | `BigDecimal` | priceAmount |
| `price_currency` | `String` | priceCurrency |
| `status` | `ProductStatus` | status (enum como texto) |
| — | `List<String>` | variants: tabla `product_variants` |

Tabla hija `product_variants`, unida por `product_id`.

### `refunds` (RefundJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `order_id` | `String` | orderId (único) |
| `amount` | `BigDecimal` | amount |
| `currency` | `String` | currency |
| `status` | `RefundStatus` | status (enum como texto) |

### `return_requests` (ReturnRequestJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `order_id` | `String` | orderId |
| `reason` | `String` | reason |
| `status` | `ReturnStatus` | status (enum como texto) |
| — | `Map<String, Integer>` | restockedUnits: tabla `return_restocked_units` |

Tabla hija `return_restocked_units`, unida por `return_id`.

### `sellers` (SellerJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `user_id` | `String` | userId (único) |

### `shipments` (ShipmentJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `order_id` | `String` | orderId (único) |
| `street` | `String` | street (dirección) |
| `city` | `String` | city (dirección) |
| `department` | `String` | department (dirección) |
| `postal_code` | `String` | postalCode (dirección) |
| `status` | `ShipmentStatus` | status (enum como texto) |

### `users` (UserJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `first_names` | `String` | firstNames |
| `last_names` | `String` | lastNames |
| `email` | `String` | email (único) |
| `document` | `String` | document (único) |
| `role` | `UserRole` | role (enum como texto) |
| `status` | `UserStatus` | status (enum como texto) |

### `warehouses` (WarehouseJpaEntity)

| Columna | Tipo Java | Campo |
|---|---|---|
| `id` | `String` | id (PK) |
| `name` | `String` | name (único) |
| `type` | `WarehouseType` | type (enum como texto) |
| `street` | `String` | street (dirección) |
| `city` | `String` | city (dirección) |
| `department` | `String` | department (dirección) |
| `postal_code` | `String` | postalCode (dirección) |
| `ownership` | `WarehouseOwnership` | ownership (enum como texto) |
| `seller_id` | `String` | sellerId |

---

## 7. Auditoría en MongoDB

La auditoría sigue el mismo patrón del proyecto de referencia (`AuditLogMongoAdapter`). Se guarda en la base `nexusmarket_audit`, colección `audit_logs`, con un documento por operación.

| Campo del documento | Tipo | Contenido |
|---|---|---|
| `_id` (`id`) | `String` | id del registro (UUID como texto) |
| `operation` | `String` | nombre del `AuditOperation`, por ejemplo `ORDER_PAYMENT` |
| `date` | `LocalDateTime` | fecha y hora de la operación |
| `performedByUserId` | `String` | usuario que la hizo |
| `performedByRole` | `String` | rol del usuario en ese momento |
| `affectedEntityId` | `String` | registro afectado (pedido, producto, usuario...) |
| `details` | `Map<String, String>` | datos adicionales, como el nuevo estado o el monto |

* **Por qué MongoDB.** La auditoría solo crece, no tiene relaciones con otras tablas y cada registro trae detalles distintos según la operación. Un documento flexible encaja mejor que una tabla con columnas fijas.
* **Independiente del dominio.** Los enums se guardan como texto y las referencias como ids, así que el registro se puede leer aunque el pedido o el usuario cambien después.
* **Consultas.** `AuditLogMongoRepository` deduce `findByPerformedByUserId` y `findByAffectedEntityId` del nombre del método.

---

## 8. Pruebas

`JpaPersistenceFlowTest` arma los seis casos de uso sobre los 14 adaptadores JPA reales y reemplaza solo los repositorios de Spring Data por `FakeSpringDataRepository`, un repositorio en memoria que deduce las consultas de su nombre (`findByOrderId`, `existsBySellerIdAndNameIgnoreCase`), como hace Spring Data. Recorre la compra física completa (con devolución, reembolso y reintegro), la compra digital, la unicidad de correo, bodega y producto, y el bloqueo de usuarios. Cada paso vuelve a leer lo que guardó el anterior, así que comprueba que mappers y adaptadores conservan ids, estados y referencias. La auditoría de ese flujo pasa por el `AuditLogMongoAdapter` real.

`AuditLogMongoAdapterTest` comprueba que el documento guarda los enums como texto, que un registro se lee exactamente como se guardó y que las búsquedas por usuario y por registro afectado funcionan.