# SDD de NexusMarket

Especificación del diseño de software de NexusMarket, construida a partir de la especificación funcional del negocio. La documentación está en español; el código Java está en inglés, y cada clase, atributo y método nombrado aquí coincide con el código en `nexusmarket/src/main/java/application/domain/`.

## Contenido

```text
SDD
└── domain
    ├── Domain Model.md            entidades: descripción, atributos, comportamiento, relaciones y reglas
    ├── Domain Value Objects.md    objetos de valor y enumeraciones con sus valores permitidos
    ├── Domain Services.md         resumen de los servicios de dominio por subdominio
    ├── services/                  detalle de cada servicio, un archivo por subdominio
    ├── Input-ports.md             puertos de entrada (casos de uso)
    └── Output-ports.md            puertos de salida (repositorios)
```

## Estructura del código

```text
domain
├── entities        16 entidades
├── valueobjects    5 objetos de valor
├── enums           15 enumeraciones
├── exceptions      AccessDeniedException
├── services        32 servicios de dominio
└── ports
    ├── in          25 puertos de entrada
    └── out         5 puertos de salida
```

Las reglas del dominio están cubiertas por 174 pruebas automáticas con JUnit en `nexusmarket/src/test/java/application/domain/services/`.
