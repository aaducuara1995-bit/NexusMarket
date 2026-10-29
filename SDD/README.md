# SDD de NexusMarket

Especificación del diseño de software de NexusMarket, construida a partir de la especificación funcional del negocio. La documentación está en español; el código Java está en inglés, y cada clase, atributo y método nombrado aquí coincide con el código en `nexusmarket/src/main/java/application/`.

## Contenido

```text
SDD
├── domain
│   ├── Domain Model.md            entidades: descripción, atributos, comportamiento, relaciones y reglas
│   ├── Domain Value Objects.md    objetos de valor y enumeraciones con sus valores permitidos
│   ├── Domain Services.md         resumen de los servicios de dominio por subdominio
│   ├── services/                  detalle de cada servicio, un archivo por subdominio
│   ├── Input-ports.md             puertos de entrada por rol
│   └── Output-ports.md            puertos de salida (repositorios)
└── Adapters
    ├── Api-rest-endpoints.md      los 47 endpoints (login y uno por rol), con cuerpos y respuestas
    ├── Rest-security.md           login, JWT, reglas por ruta y CORS
    ├── Rest-adapters.md           controladores, DTOs, mappers y decisiones de la capa REST
    ├── Global-exception-handler.md  cómo cada error se convierte en código HTTP
    ├── Use-cases-adapters.md      casos de uso que implementan los puertos por rol
    └── Persistence-adapters.md    adaptadores JPA (MySQL) y MongoDB, tablas, documentos y decisiones
```

## Estructura del código

```text
application
├── domain
│   ├── entities        17 entidades
│   ├── valueobjects    6 objetos de valor
│   ├── enums           16 enumeraciones
│   ├── exceptions      AccessDeniedException, EntityNotFoundException, InvalidCredentialsException
│   ├── services        35 servicios de dominio
│   └── ports
│       ├── in          6 puertos de entrada, uno por rol más el acceso público
│       └── out         17 puertos de salida (14 MySQL, 1 MongoDB, 2 de seguridad)
└── adapters
    ├── config          DomainServicesConfig (servicios como beans) y BootstrapAdminRunner
    ├── rest            7 controladores REST (login y uno por rol) con sus DTOs, mappers y manejo de errores
    ├── security        JWT, BCrypt, filtro de autenticación y reglas por rol
    ├── useCases        6 casos de uso, InventoryOperationsSupport y Lookup
    └── persistence
        ├── jpa         14 adaptadores MySQL con sus entidades, mappers y repositorios
        └── mongodb     adaptador de auditoría con su documento, mapper y repositorio
```

Las reglas del dominio y los flujos de los casos de uso están cubiertos por 273 pruebas automáticas con JUnit en `nexusmarket/src/test/java/application/`.

## Cómo levantar la aplicación

Todo en Docker (aplicación, MySQL y MongoDB), desde la carpeta `NexusMarket`:

```powershell
docker compose up -d --build
docker compose logs -f nexusmarket-app     # Ctrl + C para dejar de ver el log
```

O solo las bases en Docker y la aplicación desde Windows:

```powershell
docker compose up -d mysql-db mongo-db
cd nexusmarket
.\mvnw spring-boot:run
```

En los dos casos Hibernate crea las tablas, se crea la colección de auditoría y, si no hay ningún administrador, se crea el inicial.

Con la aplicación arriba, la API queda en `http://localhost:8080/api/v1/...`. Primero se inicia sesión en `POST /api/v1/auth/login` (el administrador inicial es `admin@nexusmarket.com` con `Admin12345*`) y el token se envía como `Authorization: Bearer <token>`. La lista completa está en `Adapters/Api-rest-endpoints.md`.