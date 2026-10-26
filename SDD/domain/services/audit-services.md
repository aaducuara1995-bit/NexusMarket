# Auditoría

Registro y consulta de la auditoría de operaciones, guardada en MongoDB (trazabilidad y OBJ-12).

---

## ConsultAuditLogService

**Propósito:** Consulta la auditoría completa o la de un registro específico.

**Quién lo ejecuta:** Administrador y supervisor.

**Lo usa:** `AdminUseCaseImpl`, `SupervisorUseCaseImpl`

**Dependencias:** `AuthorizationService authorization`, `AuditLogRepositoryPort auditLogs`

**Operaciones:**

```java
List<AuditLog> consultAll(User actor);
List<AuditLog> consultByEntity(User actor, UUID entityId);
```

**Reglas de negocio:**

* Exige `ADMINISTRATIVE_REPORTS`.
* Devuelve los registros del más reciente al más antiguo; si dos tienen la misma hora, primero el último guardado.

---

## RegisterAuditLogService

**Propósito:** Deja el registro de auditoría de una operación que ya terminó con éxito.

**Quién lo ejecuta:** Interno (lo usan los casos de uso después de cada operación).

**Lo usa:** `AdminUseCaseImpl`, `BuyerUseCaseImpl`, `InventoryOperationsSupport`, `LogisticOperatorUseCaseImpl`, `PublicAccessUseCaseImpl`, `SellerUseCaseImpl`

**Dependencias:** `AuditLogRepositoryPort auditLogs`

**Operaciones:**

```java
AuditLog register(AuditOperation operation, User performedBy, UUID affectedEntityId, Map<String, String> details);
```

**Reglas de negocio:**

* Exige la operación, el usuario que la hizo y el registro afectado.
* Solo se llama cuando la operación ya se completó: una operación que falla no deja registro.
* Los registros no se modifican ni se borran.