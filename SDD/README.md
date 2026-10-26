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
    ├── Use-cases-adapters.md      casos de uso que implementan los puertos por rol
    └── Persistence-adapters.md    adaptadores JPA (MySQL) y MongoDB, tablas, documentos y decisiones
```

## Estructura del código

```text
application
├── domain
│   ├── entities        17 entidades
│   ├── valueobjects    5 objetos de valor
│   ├── enums           16 enumeraciones
│   ├── exceptions      AccessDeniedException, EntityNotFoundException
│   ├── services        34 servicios de dominio
│   └── ports
│       ├── in          6 puertos de entrada, uno por rol más el acceso público
│       └── out         15 puertos de salida (14 MySQL, 1 MongoDB)
└── adapters
    ├── useCases        6 casos de uso, InventoryOperationsSupport y Lookup
    └── persistence
        ├── jpa         14 adaptadores MySQL con sus entidades, mappers y repositorios
        └── mongodb     adaptador de auditoría con su documento, mapper y repositorio
```

Las reglas del dominio y los flujos de los casos de uso están cubiertos por 237 pruebas automáticas con JUnit en `nexusmarket/src/test/java/application/`.