# Output Ports

## Introducción

Los puertos de salida son los contratos con los que el dominio pide información o servicios externos. El dominio es dueño de estas interfaces y nunca depende directamente de bases de datos, JPA, Spring ni HTTP. Sus implementaciones pertenecen a la capa de adaptadores.

```text
Domain Service
      |
      v
Output Port  (domain/ports/out)
      |
      v
Output Adapter  (adaptadores, etapa siguiente)
      |
      v
Base de datos
```

En las pruebas se usan implementaciones en memoria (`InMemoryRepositories`).

---

## BuyerRepositoryPort

Comprueba si un usuario ya tiene perfil de comprador y lo guarda. Lo usa `RegisterBuyerService`.

```java
boolean existsByUserId(UUID userId);
void save(Buyer buyer);
```

## ProductRepositoryPort

Comprueba si un vendedor ya tiene un producto con un nombre y lo guarda. Lo usa `RegisterProductService`.

```java
boolean existsNameForSeller(UUID sellerId, String name);
void save(Product product);
```

## SellerRepositoryPort

Comprueba si un usuario ya tiene perfil de vendedor y lo guarda. Lo usa `RegisterSellerService`.

```java
boolean existsByUserId(UUID userId);
void save(Seller seller);
```

## UserRepositoryPort

Consulta la unicidad de correo y documento y guarda usuarios. Lo usa `RegisterUserService`.

```java
boolean existsByEmail(Email email);
boolean existsByDocument(IdentityDocument document);
void save(User user);
```

## WarehouseRepositoryPort

Comprueba si ya existe una bodega con un nombre y la guarda. Lo usa `RegisterWarehouseService`.

```java
boolean existsByName(String name);
void save(Warehouse warehouse);
```
