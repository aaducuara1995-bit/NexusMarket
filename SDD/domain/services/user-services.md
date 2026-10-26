# Usuarios, vendedores y bodegas

Registro de usuarios y de sus perfiles, incorporación de vendedores y administración de bodegas (Dominios 1 a 4).

---

## OnboardSellerService

**Propósito:** Incorpora a un vendedor junto con su primera bodega, como describe el flujo 6.1.

**Quién lo ejecuta:** Administrador.

**Lo usa:** `AdminUseCaseImpl`

**Dependencias:** `AuthorizationService authorization`

**Operaciones:**

```java
Result onboard(User admin, User sellerUser, String warehouseName, WarehouseType warehouseType, Address warehouseAddress);
```

**Resultado:** `Result(Seller seller, Warehouse warehouse)`

**Reglas de negocio:**

* Requiere permisos de `SELLER_REGISTRATION` y `WAREHOUSE_MANAGEMENT`.
* El usuario tiene rol `SELLER` y está activo.
* La bodega queda a nombre del vendedor.

---

## RegisterBuyerService

**Propósito:** Crea el perfil de comprador de un usuario.

**Quién lo ejecuta:** Comprador.

**Lo usa:** `PublicAccessUseCaseImpl`

**Dependencias:** `BuyerRepositoryPort buyers`

**Operaciones:**

```java
Buyer register(User user, Address mainAddress);
```

**Reglas de negocio:**

* El usuario tiene rol `BUYER` y está activo.
* Un usuario tiene un solo perfil de comprador (`BuyerRepositoryPort`).
* La dirección principal es obligatoria.

---

## RegisterSellerService

**Propósito:** Crea el perfil de vendedor de un usuario.

**Quién lo ejecuta:** Administrador.

**Dependencias:** `AuthorizationService authorization`, `SellerRepositoryPort sellers`

**Operaciones:**

```java
Seller register(User admin, User sellerUser);
```

**Reglas de negocio:**

* Requiere permiso de `SELLER_REGISTRATION`.
* El usuario tiene rol `SELLER` y está activo.
* Un usuario tiene un solo perfil de vendedor (`SellerRepositoryPort`).

---

## RegisterUserService

**Propósito:** Registra un usuario nuevo.

**Quién lo ejecuta:** Comprador (registro propio) o administrador.

**Lo usa:** `AdminUseCaseImpl`, `PublicAccessUseCaseImpl`

**Dependencias:** `AuthorizationService authorization`, `UserRepositoryPort users`

**Operaciones:**

```java
User register(User actor, FullName name, Email email, IdentityDocument document, UserRole role);
```

**Reglas de negocio:**

* El correo y el documento deben ser únicos; se consultan en `UserRepositoryPort`.
* Un comprador puede registrarse solo.
* Un vendedor no se registra solo: lo registra un administrador.
* Los demás roles también los registra un administrador activo.

---

## RegisterWarehouseService

**Propósito:** Registra bodegas del Marketplace o de un vendedor.

**Quién lo ejecuta:** Administrador.

**Lo usa:** `AdminUseCaseImpl`

**Dependencias:** `AuthorizationService authorization`, `WarehouseRepositoryPort warehouses`

**Operaciones:**

```java
Warehouse registerForMarketplace(User admin, String name, WarehouseType type, Address address);
Warehouse registerForSeller(User admin, Seller seller, String name, WarehouseType type, Address address);
```

**Reglas de negocio:**

* Requiere permiso de `WAREHOUSE_MANAGEMENT`.
* El nombre de la bodega es único (`WarehouseRepositoryPort`).
* Una bodega física exige dirección.