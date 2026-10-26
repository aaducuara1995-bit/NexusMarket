# Output Ports

## Introducción

Los puertos de salida son los contratos con los que el dominio pide información o servicios externos. El dominio es dueño de estas interfaces y nunca depende directamente de bases de datos, JPA, Spring ni HTTP. Sus implementaciones pertenecen a la capa de adaptadores de persistencia.

```text
Use Case Adapter / Domain Service
      |
      v
Output Port  (domain/ports/out)
      |
      v
Persistence Adapter  (adapters/persistence, Fase B: MySQL)
      |
      v
Base de datos
```

Hay 14 puertos, uno por entidad que se guarda. Las búsquedas devuelven `Optional` cuando puede no haber resultado y `List` cuando puede haber varios. En las pruebas se usan implementaciones en memoria (`InMemoryRepositories`).

---

## BuyerRepositoryPort

Perfiles de comprador, buscados por id o por el usuario al que pertenecen.

**Lo usan:** `BuyerUseCaseImpl`, `RegisterBuyerService`

```java
boolean existsByUserId(UUID userId);
Optional<Buyer> findById(UUID id);
Optional<Buyer> findByUserId(UUID userId);
void save(Buyer buyer);
```

## CartRepositoryPort

Carrito activo de cada comprador.

**Lo usan:** `BuyerUseCaseImpl`

```java
Optional<Cart> findByBuyer(UUID buyerId);
void save(Cart cart);
```

## InventoryMovementRepositoryPort

Historial de movimientos de inventario (trazabilidad, RN-04).

**Lo usan:** `BuyerUseCaseImpl`, `InventoryOperationsSupport`, `LogisticOperatorUseCaseImpl`

```java
List<InventoryMovement> findByInventory(UUID inventoryId);
void saveAll(List<InventoryMovement> movements);
```

## InventoryRepositoryPort

Inventario por producto y bodega.

**Lo usan:** `BuyerUseCaseImpl`, `InventoryOperationsSupport`, `LogisticOperatorUseCaseImpl`, `PublicAccessUseCaseImpl`, `SellerUseCaseImpl`, `SupervisorUseCaseImpl`

```java
Optional<Inventory> findById(UUID id);
List<Inventory> findByProduct(UUID productId);
List<Inventory> findAll();
void save(Inventory inventory);
void saveAll(List<Inventory> inventories);
```

## InventoryReservationRepositoryPort

Reservas de stock de cada pedido.

**Lo usan:** `BuyerUseCaseImpl`, `LogisticOperatorUseCaseImpl`

```java
List<InventoryReservation> findByOrder(UUID orderId);
void saveAll(List<InventoryReservation> reservations);
```

## InvoiceRepositoryPort

Factura de cada pedido.

**Lo usan:** `BuyerUseCaseImpl`

```java
Optional<Invoice> findByOrder(UUID orderId);
void save(Invoice invoice);
```

## OrderRepositoryPort

Pedidos, por id, por comprador o todos.

**Lo usan:** `BuyerUseCaseImpl`, `LogisticOperatorUseCaseImpl`, `SupervisorUseCaseImpl`

```java
Optional<Order> findById(UUID id);
List<Order> findByBuyer(UUID buyerId);
List<Order> findAll();
void save(Order order);
```

## ProductRepositoryPort

Productos: unicidad del nombre por vendedor, productos de un vendedor y catálogo publicado.

**Lo usan:** `BuyerUseCaseImpl`, `InventoryOperationsSupport`, `PublicAccessUseCaseImpl`, `RegisterProductService`, `SellerUseCaseImpl`

```java
boolean existsNameForSeller(UUID sellerId, String name);
Optional<Product> findById(UUID id);
List<Product> findBySeller(UUID sellerId);
List<Product> findPublished();
void save(Product product);
```

## RefundRepositoryPort

Reembolsos, por id o por pedido.

**Lo usan:** `AdminUseCaseImpl`

```java
Optional<Refund> findById(UUID id);
Optional<Refund> findByOrder(UUID orderId);
void save(Refund refund);
```

## ReturnRequestRepositoryPort

Solicitudes de devolución de cada pedido.

**Lo usan:** `AdminUseCaseImpl`, `BuyerUseCaseImpl`, `InventoryOperationsSupport`

```java
Optional<ReturnRequest> findById(UUID id);
List<ReturnRequest> findByOrder(UUID orderId);
void save(ReturnRequest returnRequest);
```

## SellerRepositoryPort

Perfiles de vendedor, buscados por id o por el usuario al que pertenecen.

**Lo usan:** `AdminUseCaseImpl`, `RegisterSellerService`, `SellerUseCaseImpl`

```java
boolean existsByUserId(UUID userId);
Optional<Seller> findById(UUID id);
Optional<Seller> findByUserId(UUID userId);
void save(Seller seller);
```

## ShipmentRepositoryPort

Envío de cada pedido.

**Lo usan:** `BuyerUseCaseImpl`, `LogisticOperatorUseCaseImpl`

```java
Optional<Shipment> findByOrder(UUID orderId);
void save(Shipment shipment);
```

## UserRepositoryPort

Usuarios de la plataforma: unicidad de correo y documento, búsqueda y guardado.

**Lo usan:** `AdminUseCaseImpl`, `RegisterUserService`, `SupervisorUseCaseImpl`

```java
boolean existsByEmail(Email email);
boolean existsByDocument(IdentityDocument document);
Optional<User> findById(UUID id);
Optional<User> findByEmail(Email email);
List<User> findAll();
void save(User user);
```

## WarehouseRepositoryPort

Bodegas del marketplace y de los vendedores; el nombre es único.

**Lo usan:** `AdminUseCaseImpl`, `InventoryOperationsSupport`, `RegisterWarehouseService`, `SupervisorUseCaseImpl`

```java
boolean existsByName(String name);
Optional<Warehouse> findById(UUID id);
List<Warehouse> findAll();
void save(Warehouse warehouse);
```