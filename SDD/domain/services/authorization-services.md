# Autorización y validación

Servicios de apoyo que deciden quién puede ejecutar cada operación. Los demás servicios los usan antes de cambiar cualquier entidad. Aplican RG-01, RG-02, RG-03 y la matriz de responsabilidades (sección 12).

---

## AuthorizationService

**Propósito:** Decide si un usuario puede ejecutar un proceso de negocio según su rol y su estado.

**Quién lo ejecuta:** Interno (lo usan otros servicios).

**Lo usa:** `AdminUseCaseImpl`, `BuyerUseCaseImpl`, `LogisticOperatorUseCaseImpl`, `SellerUseCaseImpl`, `SupervisorUseCaseImpl`

**Dependencias:** ninguna

**Operaciones:**

```java
boolean canExecute(User user, BusinessProcess process);
void requirePermission(User user, BusinessProcess process);
```

**Reglas de negocio:**

* Un usuario bloqueado no puede ejecutar ninguna operación.
* Cada `BusinessProcess` tiene una lista fija de roles permitidos, tomada de la matriz de responsabilidades.
* El `SUPERVISOR` solo tiene `ADMINISTRATIVE_REPORTS`: su perfil es de consulta.
* Supuesto: `RETURN_MANAGEMENT` se asigna como `REFUND_MANAGEMENT` (comprador y administrador), porque la especificación no lo define.

---

## BuyerValidationService

**Propósito:** Comprueba que un usuario pueda comprar con un perfil de comprador.

**Quién lo ejecuta:** Interno.

**Dependencias:** ninguna

**Operaciones:**

```java
boolean canPurchase(User user, Buyer buyer);
void requireEnabled(User user, Buyer buyer);
```

**Reglas de negocio:**

* El usuario tiene rol `BUYER` y está activo.
* El `Buyer` pertenece a ese usuario.
* El estado comercial es `ENABLED`.

---

## InventoryAccessService

**Propósito:** Decide quién puede administrar un inventario.

**Quién lo ejecuta:** Interno.

**Dependencias:** `AuthorizationService authorization`, `SellerValidationService sellerValidation`

**Operaciones:**

```java
void requireAccess(User actor, Seller seller, Product product);
```

**Reglas de negocio:**

* Requiere permiso de `INVENTORY_MANAGEMENT` (vendedor u operador logístico).
* Un vendedor solo administra el inventario de sus propios productos.
* El operador logístico opera cualquier bodega.

---

## SellerValidationService

**Propósito:** Comprueba que un usuario actúe como vendedor y, cuando aplica, que sea dueño del producto.

**Quién lo ejecuta:** Interno.

**Dependencias:** ninguna

**Operaciones:**

```java
void requireSeller(User user, Seller seller);
void requireOwner(User user, Seller seller, Product product);
```

**Reglas de negocio:**

* El usuario tiene rol `SELLER` y está activo.
* El `Seller` pertenece a ese usuario.
* El producto pertenece a ese vendedor.