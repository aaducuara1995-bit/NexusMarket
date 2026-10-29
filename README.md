# NexusMarket — Construcción de Software 2 (2026-2)

Marketplace donde compradores y vendedores hacen negocios: catálogo, inventario distribuido por bodegas, carrito, pedidos, pago con factura, envíos y posventa (devoluciones y reembolsos). Está hecho en **Java 17 + Spring Boot 4.1** con **arquitectura hexagonal** (DDD + puertos y adaptadores), a partir de la especificación funcional del negocio y siguiendo el proyecto de referencia del curso. Usa **MySQL** para los datos del negocio y **MongoDB** para la auditoría.

> **Para poner el proyecto en marcha paso a paso, ver [`SETUP.md`](SETUP.md).**

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje / build | Java 17, Maven (wrapper `nexusmarket/mvnw.cmd` o contenedor Docker) |
| Framework | Spring Boot 4.1, Spring MVC, Spring Security (JWT sin sesión, BCrypt) |
| Persistencia relacional | Spring Data JPA / Hibernate → MySQL 8 (`nexusmarket_db`, tablas automáticas) |
| Auditoría NoSQL | Spring Data MongoDB → MongoDB 6 (`nexusmarket_audit.audit_logs`) |
| JWT | JJWT 0.12.6, HS256, vence en una hora |
| Validación y errores | Jakarta Bean Validation y `@RestControllerAdvice` con un solo formato de error |
| Pruebas | JUnit 5: dominio, casos de uso, persistencia, REST y seguridad |

## Estructura del repositorio

```text
.
├── nexusmarket/              # Aplicación Spring Boot (código, pruebas y recursos)
│   ├── Dockerfile            # Java 17 + Maven para correr la app en un contenedor
│   └── src/main/java/application/
│       ├── domain/           # Núcleo puro, sin Spring: entities, valueobjects, enums,
│       │                     #   exceptions, services, ports/in (5 roles + público), ports/out
│       └── adapters/         # useCases (implementan los puertos de entrada),
│                             #   persistence (jpa para MySQL, mongodb para auditoría),
│                             #   rest (controladores, DTOs, mappers, errores),
│                             #   security (JWT, filtro, BCrypt, reglas por rol), config
├── SDD/                      # Diseño: dominio (modelo, objetos de valor, servicios, puertos)
│                             #   y adaptadores (casos de uso, persistencia, REST, seguridad)
├── postman/                  # Colección con el flujo completo y los errores esperados
├── docker-compose.yml        # App + MySQL (3307) + MongoDB (27018)
├── SETUP.md                  # Guía paso a paso para ejecutar el proyecto
└── LICENSE
```

## Roles y seguridad

- Inicio de sesión en `POST /api/v1/auth/login` con correo y contraseña. Devuelve un JWT que va en cada petición como `Authorization: Bearer <token>`.
- Cinco roles, cada uno con su puerto de entrada y su ruta: comprador (`/api/v1/buyer`), vendedor (`/api/v1/seller`), operador logístico (`/api/v1/logistics`), administrador (`/api/v1/admin`) y supervisor (`/api/v1/supervisor`). El acceso público (`/api/v1/public`) permite registrarse como comprador y ver el catálogo.
- Cada ruta solo acepta tokens de su rol. Además, el dominio vuelve a revisar los permisos (`AuthorizationService`) y la propiedad de los recursos: un vendedor no toca los productos de otro.
- Las contraseñas se guardan cifradas con BCrypt y nunca viajan en el token.
- Códigos HTTP: 200 y 201 en éxito; 400 datos inválidos; 401 sin token o credenciales equivocadas; 403 rol sin permiso; 404 no existe; 409 regla de negocio rechazada (sin stock, pedido ya pagado...).
- Al arrancar se crea el administrador inicial: `admin@nexusmarket.com` / `Admin12345*`.

## Variables de entorno

| Variable | Por defecto | Uso |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3307/nexusmarket_db?...` | MySQL (en Compose: `mysql-db:3306`) |
| `DB_USERNAME` / `DB_PASSWORD` | `nexus_app` / `nexus_app_password` | Credenciales de MySQL |
| `MONGODB_URI` | `mongodb://localhost:27018/nexusmarket_audit` | MongoDB (en Compose: `mongo-db:27017`) |
| `JWT_SECRET` | `TDEATDEATDEATDEATDEATDEATDEATDEA` | Clave de firma del JWT. Es académica y pública: cámbiala si expones el servicio |
| `JWT_EXPIRATION_MS` | `3600000` | Vigencia del token |
| `FRONTEND_ORIGIN` | `http://localhost:5173` | Origen CORS permitido |
| `SERVER_PORT` | `8080` | Puerto HTTP de la aplicación |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | `admin@nexusmarket.com` / `Admin12345*` | Administrador inicial |

Los puertos 3307 y 27018 en el computador evitan choques con un MySQL o un MongoDB instalados en Windows.

## Documentación

- `SDD/README.md`: índice del diseño.
- `SDD/domain/`: modelo de dominio, objetos de valor y enums, servicios de dominio, puertos de entrada y de salida.
- `SDD/Adapters/`: casos de uso, persistencia (tablas y colección), API REST (47 endpoints), manejo de errores y seguridad.

## Licencia

MIT, ver [`LICENSE`](LICENSE).
