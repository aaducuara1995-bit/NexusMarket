# Domain Services

## Introducción

Los servicios de dominio contienen las reglas de negocio que no caben en una sola entidad, por ejemplo repartir la reserva de un pedido entre varias bodegas o despachar un pedido actualizando a la vez el envío, el pedido y el inventario. No dependen de bases de datos, frameworks ni HTTP: reciben entidades del dominio y, cuando necesitan información externa, usan un puerto de salida.

Hay 32 servicios en `domain/services/`, organizados por subdominio. El detalle de cada uno (operaciones, dependencias, rol que lo ejecuta y reglas) está en la carpeta `services/`.

Las operaciones que modifican varias entidades validan todo antes de cambiar algo, y las que reservan stock son de todo o nada: si algo falla a mitad de camino, se deshace lo que ya se había hecho.

---

# Autorización y validación

Servicios de apoyo que deciden quién puede ejecutar cada operación. Los demás servicios los usan antes de cambiar cualquier entidad. Aplican RG-01, RG-02, RG-03 y la matriz de responsabilidades (sección 12).

Detalle: `services/authorization-services.md`

## AuthorizationService

Decide si un usuario puede ejecutar un proceso de negocio según su rol y su estado.

## BuyerValidationService

Comprueba que un usuario pueda comprar con un perfil de comprador.

## InventoryAccessService

Decide quién puede administrar un inventario.

## SellerValidationService

Comprueba que un usuario actúe como vendedor y, cuando aplica, que sea dueño del producto.

---

# Usuarios, vendedores y bodegas

Registro de usuarios y de sus perfiles, incorporación de vendedores y administración de bodegas (Dominios 1 a 4).

Detalle: `services/user-services.md`

## OnboardSellerService

Incorpora a un vendedor junto con su primera bodega, como describe el flujo 6.1.

## RegisterBuyerService

Crea el perfil de comprador de un usuario.

## RegisterSellerService

Crea el perfil de vendedor de un usuario.

## RegisterUserService

Registra un usuario nuevo.

## RegisterWarehouseService

Registra bodegas del Marketplace o de un vendedor.

---

# Catálogo

Registro y ciclo de vida de los productos (Dominio 5).

Detalle: `services/catalog-services.md`

## ChangeProductStatusService

Suspende o descontinúa un producto.

## CheckAvailabilityService

Calcula cuántas unidades de un producto hay disponibles entre todas las bodegas.

## PublishProductService

Publica un producto en el catálogo.

## RegisterProductService

Registra un producto nuevo de un vendedor.

---

# Inventario

Existencias distribuidas por bodega y sus cinco movimientos: ingreso, reserva, salida por venta, ajuste y devolución (Dominio 6).

Detalle: `services/inventory-services.md`

## AdjustInventoryService

Ajusta las existencias de un inventario con un motivo.

## InventoryOutflowService

Convierte las reservas de un pedido en salida definitiva.

## MarkInventoryService

Marca un inventario como dañado, lo bloquea o lo habilita.

## RegisterInitialInventoryService

Registra las existencias iniciales de un producto en una bodega.

## ReleaseReservationService

Deshace las reservas de un pedido que no se concreta.

## ReserveInventoryService

Aparta stock de un producto para un pedido, repartiéndolo entre varias bodegas si hace falta.

## RestockInventoryService

Reintegra al inventario las unidades de una devolución aprobada.

---

# Carrito y pedidos

Desde la selección de productos hasta el pago y la entrega de productos digitales (OBJ-07, OBJ-08 y Dominio 7).

Detalle: `services/order-services.md`

## AddToCartService

Agrega un producto al carrito del comprador.

## ConfirmOrderService

Convierte el carrito en un pedido y reserva el stock de cada producto.

## DeliverDigitalOrderService

Entrega de inmediato un pedido pagado formado solo por productos digitales.

## PayOrderService

Registra el pago de un pedido.

---

# Facturación y logística

Factura, envío, despacho y confirmación de entrega (OBJ-09 y OBJ-10).

Detalle: `services/logistics-billing-services.md`

## ConfirmDeliveryService

Confirma la entrega y finaliza el pedido.

## CreateShipmentService

Crea el envío de un pedido pagado con productos físicos.

## IssueInvoiceService

Emite la factura de un pedido pagado.

## ShipOrderService

Despacha un pedido: confirma la salida de inventario y actualiza envío y pedido a la vez.

---

# Posventa

Devoluciones y reembolsos (OBJ-11).

Detalle: `services/after-sales-services.md`

## GenerateRefundService

Genera el reembolso de una devolución aprobada.

## ProcessRefundService

Procesa un reembolso pendiente.

## RequestReturnService

Registra la solicitud de devolución de un pedido.

## ResolveReturnService

Aprueba o rechaza una devolución.
