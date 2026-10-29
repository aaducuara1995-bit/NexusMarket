# REST Adapters

## 1. Descripción general

Los adaptadores REST están en `adapters/rest/`. Reciben las peticiones HTTP, las convierten en objetos del dominio, llaman al puerto de entrada del rol y convierten la respuesta en JSON. No contienen reglas de negocio.

```text
Postman / cliente
      |  JSON + Authorization: Bearer <token>
      v
JwtAuthenticationFilter + SecurityConfig (adapters/security): token válido y rol de la ruta
      v
XxxRestController (adapters/rest/controllers)
      |  1. @Valid revisa el cuerpo (RequestDTO)
      |  2. @AuthenticationPrincipal -> User reconstruido desde el JWT
      |  3. RestReferenceLoader -> objetos del dominio a partir de los ids de la URL
      |  4. XxxRestMapper -> objetos nuevos del dominio a partir del cuerpo
      v
Role Input Port (domain/ports/in) -> Use Case Adapter -> Domain Services
      |
      v
XxxRestMapper -> ResponseDTO -> JSON
```

---

## 2. Estructura

```text
adapters/rest/
├── controllers/     AuthRestController (login) y 6 controladores, uno por rol
├── dtos/
│   ├── AddressDTO          dirección, en peticiones y respuestas
│   ├── requests/           cuerpos de entrada (records con validación)
│   └── responses/          cuerpos de salida (records)
├── mappers/         XxxRestMapper: DTO <-> dominio
├── exception/       GlobalExceptionHandler
└── RestReferenceLoader.java         ids de la URL -> objetos del dominio
```

Los DTO son `record` de Java: 13 de petición y 16 de respuesta, más `AddressDTO`. Ningún controlador expone una entidad del dominio directamente.

---

## 3. Decisiones de diseño

* **Un controlador por rol.** Cada uno inyecta solo el puerto de su rol (`BuyerPort`, `SellerPort`...). Así un endpoint de comprador no puede llamar por error una operación de administrador.
* **Usuario de la petición.** Cada método recibe `@AuthenticationPrincipal(expression = "user") User user`, igual que el proyecto de referencia: el `User` del dominio reconstruido desde el JWT por `JwtAuthenticationFilter`. El controlador nunca lee el token ni consulta la base de datos para saber quién llama.
* **Ids de la URL.** El proyecto de referencia arma modelos "de referencia" con setters y solo el id. Las entidades de NexusMarket no tienen setters ni se pueden crear con solo el id, porque protegen sus reglas. Por eso `RestReferenceLoader` las lee por id a través de los puertos de salida; un id que no existe da 404. El caso de uso las vuelve a cargar dentro de su transacción, como siempre.
* **Objetos nuevos desde el cuerpo.** Para registrar un producto, un usuario o una bodega, el mapper arma un borrador del dominio con los datos del cuerpo. El caso de uso toma esos datos y crea el objeto definitivo (por ejemplo, el producto queda a nombre del vendedor que hace la petición, no del que diga el borrador).
* **Códigos de respuesta.** 201 cuando se crea algo (usuario, comprador, vendedor, bodega, producto, inventario, pedido, devolución) y 200 en lo demás.
* **Validación en dos niveles.** `@Valid` revisa la forma de los datos (campos obligatorios, correo válido, cantidades positivas) antes de llegar al dominio. Las reglas de negocio (stock suficiente, dirección del comprador, monto exacto) las sigue revisando el dominio.

---

## 4. Seguridad

La autenticación y los permisos por ruta están en `adapters/security/` y se describen en `Rest-security.md`. Antes de llegar a un controlador, la petición ya pasó por el filtro JWT y por la regla de su ruta. Además, el dominio vuelve a revisar los permisos con `AuthorizationService`, así que una regla nunca depende solo de la configuración web.

---

## 5. Pruebas

`RestControllersPhaseC1Test`, `RestControllersPhaseC2Test` y `AuthRestControllerTest` llaman los métodos de los controladores como Java normal, con los casos de uso reales y repositorios en memoria, pasando el `User` como lo inyecta el token. Revisan el login, códigos HTTP, cuerpos de respuesta, que cada rol quede en sus endpoints y que el negocio completo funcione solo con la API: alta del vendedor, catálogo, inventario, compra, pago, despacho, entrega, devolución, reembolso y reintegro.