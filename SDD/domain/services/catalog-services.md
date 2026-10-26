# Catálogo

Registro y ciclo de vida de los productos (Dominio 5).

---

## ChangeProductStatusService

**Propósito:** Suspende o descontinúa un producto.

**Quién lo ejecuta:** Vendedor dueño del producto.

**Lo usa:** `SellerUseCaseImpl`

**Dependencias:** `AuthorizationService authorization`, `SellerValidationService sellerValidation`

**Operaciones:**

```java
void suspend(User actor, Seller seller, Product product);
void discontinue(User actor, Seller seller, Product product, List<Inventory> inventories);
```

**Reglas de negocio:**

* No se descontinúa un producto con reservas activas en pedidos en curso.
* Un producto descontinuado no puede suspenderse.

---

## CheckAvailabilityService

**Propósito:** Calcula cuántas unidades de un producto hay disponibles entre todas las bodegas.

**Quién lo ejecuta:** Cualquier usuario (consulta).

**Lo usa:** `PublicAccessUseCaseImpl`

**Dependencias:** ninguna

**Operaciones:**

```java
int availableOf(Product product, List<Inventory> inventories);
boolean hasAvailability(Product product, int quantity, List<Inventory> inventories);
```

**Reglas de negocio:**

* Suma solo inventarios `AVAILABLE` y descuenta lo reservado.
* Un producto digital siempre tiene disponibilidad.

---

## PublishProductService

**Propósito:** Publica un producto en el catálogo.

**Quién lo ejecuta:** Vendedor dueño del producto.

**Lo usa:** `SellerUseCaseImpl`

**Dependencias:** `AuthorizationService authorization`, `SellerValidationService sellerValidation`

**Operaciones:**

```java
void publish(User actor, Seller seller, Product product, List<Inventory> inventories);
```

**Reglas de negocio:**

* Un producto físico necesita existencias disponibles; uno digital no.
* No se cuenta el inventario dañado ni el reservado.
* Un producto descontinuado no se publica.

---

## RegisterProductService

**Propósito:** Registra un producto nuevo de un vendedor.

**Quién lo ejecuta:** Vendedor.

**Lo usa:** `SellerUseCaseImpl`

**Dependencias:** `AuthorizationService authorization`, `SellerValidationService sellerValidation`, `ProductRepositoryPort products`

**Operaciones:**

```java
Product register(User actor, Seller seller, String name, String description, ProductType type, Money price);
```

**Reglas de negocio:**

* Requiere permiso de `PRODUCT_REGISTRATION`.
* El nombre no se repite dentro del mismo vendedor (`ProductRepositoryPort`).
* El precio es mayor que cero.
* El producto nace `SUSPENDED`.