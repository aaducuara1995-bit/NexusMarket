# Use Cases Adapters

## 1. Descripción general

Los adaptadores de casos de uso están en `adapters/useCases/`. Son las implementaciones concretas de los puertos de entrada por rol de `domain/ports/in/` y conectan las llamadas que llegan desde afuera (los controladores REST de la Fase C) con los servicios de dominio.

No contienen reglas de negocio. Cada operación sigue siempre los mismos pasos: **busca** en los repositorios los datos que necesita, **llama** al servicio de dominio que tiene la regla y **guarda** el resultado.

---

## 2. Rol en la arquitectura

```text
REST Controller (adapters/rest, Fase C)
      |
      v  llama al puerto
Role Input Port (domain/ports/in)
      ^
      |  implementado por
Use Case Adapter (adapters/useCases)
      |
      +--> Domain Service (domain/services): reglas de negocio
      |
      +--> Output Port (domain/ports/out): leer y guardar
```

---

## 3. Clases y dependencias

| Puerto | Clase | Servicios inyectados | Repositorios inyectados |
|---|---|---|---|
| `PublicAccessPort` | `PublicAccessUseCaseImpl` | `RegisterUserService`, `RegisterBuyerService`, `CheckAvailabilityService` | `ProductRepositoryPort`, `InventoryRepositoryPort` |
| `BuyerPort` | `BuyerUseCaseImpl` | `AuthorizationService`, `AddToCartService`, `ConfirmOrderService`, `PayOrderService`, `IssueInvoiceService`, `DeliverDigitalOrderService`, `CreateShipmentService`, `RequestReturnService` | `BuyerRepositoryPort`, `CartRepositoryPort`, `ProductRepositoryPort`, `InventoryRepositoryPort`, `InventoryReservationRepositoryPort`, `InventoryMovementRepositoryPort`, `OrderRepositoryPort`, `InvoiceRepositoryPort`, `ShipmentRepositoryPort`, `ReturnRequestRepositoryPort` |
| `SellerPort` | `SellerUseCaseImpl` | `AuthorizationService`, `RegisterProductService`, `PublishProductService`, `ChangeProductStatusService`, `InventoryOperationsSupport` | `SellerRepositoryPort`, `ProductRepositoryPort`, `InventoryRepositoryPort` |
| `LogisticOperatorPort` | `LogisticOperatorUseCaseImpl` | `AuthorizationService`, `ShipOrderService`, `ConfirmDeliveryService`, `InventoryOperationsSupport` | `OrderRepositoryPort`, `ShipmentRepositoryPort`, `InventoryReservationRepositoryPort`, `InventoryMovementRepositoryPort`, `InventoryRepositoryPort` |
| `AdminPort` | `AdminUseCaseImpl` | `AuthorizationService`, `RegisterUserService`, `OnboardSellerService`, `RegisterWarehouseService`, `ResolveReturnService`, `GenerateRefundService`, `ProcessRefundService` | `UserRepositoryPort`, `SellerRepositoryPort`, `WarehouseRepositoryPort`, `ReturnRequestRepositoryPort`, `RefundRepositoryPort` |
| `SupervisorPort` | `SupervisorUseCaseImpl` | `AuthorizationService` | `UserRepositoryPort`, `WarehouseRepositoryPort`, `InventoryRepositoryPort`, `OrderRepositoryPort` |

Clases de apoyo:

* `InventoryOperationsSupport`: las seis operaciones de inventario que comparten el vendedor y el operador logístico. El vendedor envía su perfil de `Seller` para que se valide que el producto es suyo; el operador envía `null` porque puede operar sobre cualquier producto. Inyecta `RegisterInitialInventoryService`, `AdjustInventoryService`, `MarkInventoryService`, `RestockInventoryService`, `ProductRepositoryPort`, `WarehouseRepositoryPort`, `InventoryRepositoryPort`, `InventoryMovementRepositoryPort`, `ReturnRequestRepositoryPort`.
* `Lookup`: recarga desde su repositorio cada objeto que llega a un caso de uso. La capa REST solo conoce identificadores, así que esto garantiza que el objeto existe y que se trabaja con el estado guardado. Si no existe, lanza `EntityNotFoundException`.

---

## 4. Flujos que orquestan

Algunas operaciones encadenan varios servicios de dominio. Estas son las decisiones de negocio que quedaron en esa orquestación:

* **`BuyerUseCaseImpl.confirmOrder`.** Reúne el inventario de los productos del carrito y llama a `ConfirmOrderService` con la dirección elegida (o `null` para usar la principal). Guarda el pedido, las reservas, los movimientos, el inventario y el carrito vacío.
* **`BuyerUseCaseImpl.payOrder`.** Registra el pago con `PayOrderService` y emite la factura con `IssueInvoiceService`. Si el pedido es solo digital, lo entrega de inmediato con `DeliverDigitalOrderService`; si no, crea el envío con `CreateShipmentService` hacia la dirección guardada en el pedido.
* **`LogisticOperatorUseCaseImpl.consultOrdersToShip`.** Devuelve los pedidos `PAID` que tienen productos físicos.
* **`LogisticOperatorUseCaseImpl.shipOrder`.** Busca el envío y las reservas del pedido, despacha con `ShipOrderService` y guarda el inventario, las reservas confirmadas y los movimientos `SALE`.
* **`AdminUseCaseImpl.onboardSeller`.** Verifica que el usuario no tenga ya perfil de vendedor y que el nombre de la bodega esté libre; luego crea vendedor y primera bodega con `OnboardSellerService` (RN-01).
* **`AdminUseCaseImpl.approveReturn`.** Aprueba la devolución con `ResolveReturnService` y genera el reembolso con `GenerateRefundService`, que queda `PENDING` hasta que se procese.
* **`AdminUseCaseImpl.blockUser`.** Exige `USER_MANAGEMENT` y no permite que un administrador se bloquee a sí mismo.
* **`PublicAccessUseCaseImpl.registerBuyer`.** Crea el usuario con `RegisterUserService` (sin actor solo acepta el rol `BUYER`) y su perfil con `RegisterBuyerService`.
* **`SupervisorUseCaseImpl`.** Todas sus consultas exigen `ADMINISTRATIVE_REPORTS`; ninguna modifica datos.

---

## 5. Ejemplo del patrón

```java
@Override
public Order shipOrder(User user, Order order) {
    authorizationService.requirePermission(user, BusinessProcess.SHIPPING_OPERATIONS);
    Order stored = Lookup.reload(order, Order::getId, orderRepository::findById, "Order");      // buscar
    Shipment shipment = shipmentOf(stored);
    List<InventoryReservation> reservations = reservationRepository.findByOrder(stored.getId());

    List<InventoryMovement> movements = shipOrderService.ship(user, stored, shipment, reservations); // regla

    inventoryRepository.saveAll(...);                                                            // guardar
    reservationRepository.saveAll(reservations);
    movementRepository.saveAll(movements);
    shipmentRepository.save(shipment);
    orderRepository.save(stored);
    return stored;
}
```

---

## 6. Pendiente para la Fase B

Las clases todavía no tienen `@Service`, porque Spring necesita las implementaciones reales de los repositorios para construirlas. En la Fase B se agregan los adaptadores de MySQL, las anotaciones y una clase de configuración que registra los servicios de dominio como beans, sin poner anotaciones de Spring dentro de `domain`.

Mientras tanto, las pruebas de `src/test/java/application/adapters/useCases/` arman los seis casos de uso con los servicios reales y repositorios en memoria, y recorren el flujo completo del negocio.