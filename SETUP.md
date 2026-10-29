# Guía paso a paso: poner en marcha NexusMarket

Probada en Windows 11 con PowerShell. Los comandos se ejecutan desde la raíz del repositorio (`NexusMarket`), salvo que se indique otra cosa.

## 0. Prerrequisitos

1. **Docker Desktop** abierto y con el motor corriendo ("Engine running"). Compruébalo con:
   ```powershell
   docker --version
   docker compose version
   ```
   Java 17 y Maven vienen dentro del contenedor de la aplicación, así que no hace falta instalarlos en Windows para ejecutarla.
2. Puertos libres en el computador: **3307** (MySQL), **27018** (MongoDB) y **8080** (aplicación). Para revisarlos:
   ```powershell
   Get-NetTCPConnection -LocalPort 3307,27018,8080 -State Listen -ErrorAction SilentlyContinue
   ```
   Si no muestra nada, están libres. Si el 8080 está ocupado, define `$env:HOST_SERVER_PORT="8081"` antes de levantar Compose.
3. **Postman**, opcional, para probar la API con la colección del repositorio.

## 1. Levantar la aplicación y las bases de datos

```powershell
docker compose up -d --build
docker compose ps
```

Se crean tres contenedores: `nexusmarket-mysql`, `nexusmarket-mongo` y `nexusmarket-app`. Las bases deben quedar en estado `healthy`. La aplicación arranca cuando las dos responden. La primera vez tarda varios minutos, porque descarga las imágenes y las dependencias de Maven.

Para seguir el arranque:

```powershell
docker compose logs -f nexusmarket-app
```

Está lista cuando aparece `Started NexusApplication`. La primera vez también aparece `initial administrator created with email admin@nexusmarket.com`. Para dejar de ver el log, `Ctrl + C`; la aplicación sigue corriendo.

## 2. Correr las pruebas

Dentro del contenedor, donde la aplicación ya tiene sus bases:

```powershell
docker compose exec nexusmarket-app mvn test
```

Resultado esperado: `BUILD SUCCESS`. `NexusApplicationTests.contextLoads` arranca la aplicación completa, así que necesita MySQL y MongoDB arriba (paso 1). Las demás pruebas no usan bases de datos: trabajan con repositorios en memoria.

Desde Windows, sin Docker:

```powershell
cd nexusmarket
.\mvnw test "-Dtest=*Test"
```

Este filtro corre todas las pruebas salvo `contextLoads`.

## 3. Probar la API

### Con Postman

1. **Import** → `postman/NexusMarket.postman_collection.json`.
2. En la colección **NexusMarket API** → **Run collection** → **Run**.

La colección inicia sesión con el administrador, crea un vendedor, un operador, un supervisor y un comprador, y recorre todo el negocio: producto, inventario, compra, pago, despacho, entrega, devolución, reembolso, reintegro y auditoría. La carpeta `7. Errores esperados` prueba los códigos 400, 401, 403, 404 y 409. Cada corrida usa correos nuevos, así que se puede repetir.

### Con PowerShell

```powershell
$api = "http://localhost:8080/api/v1"

# Login del administrador
$login = Invoke-RestMethod -Method Post -Uri "$api/auth/login" -ContentType "application/json" `
  -Body '{"email":"admin@nexusmarket.com","password":"Admin12345*"}'
$admin = @{ Authorization = "Bearer $($login.token)" }

# Registrar una supervisora con su contraseña
Invoke-RestMethod -Method Post -Uri "$api/admin/users" -Headers $admin -ContentType "application/json" `
  -Body '{"firstNames":"Laura","lastNames":"Gomez","email":"laura@nexus.com","document":"2000","password":"Clave12345*","role":"SUPERVISOR"}'

# La supervisora inicia sesión y consulta la auditoría
$sup = Invoke-RestMethod -Method Post -Uri "$api/auth/login" -ContentType "application/json" `
  -Body '{"email":"laura@nexus.com","password":"Clave12345*"}'
Invoke-RestMethod -Uri "$api/supervisor/audit-logs" -Headers @{ Authorization = "Bearer $($sup.token)" }
```

Sin token, las rutas protegidas responden **401**. Con el token de otro rol, **403**. La lista completa de endpoints está en `SDD/Adapters/Api-rest-endpoints.md`.

## 4. Revisar las bases de datos

Tablas de MySQL y usuarios:

```powershell
docker exec nexusmarket-mysql mysql -unexus_app -pnexus_app_password nexusmarket_db -e "SHOW TABLES; SELECT email, role, status FROM users;"
```

Registros de auditoría en MongoDB:

```powershell
docker exec nexusmarket-mongo mongosh --quiet nexusmarket_audit --eval "db.audit_logs.countDocuments()"
```

También se puede abrir MongoDB Compass con `mongodb://localhost:27018` y revisar `nexusmarket_audit` → `audit_logs`. La colección aparece con la primera operación que deja auditoría.

## 5. Detener

```powershell
docker compose down
```

Los datos quedan guardados en los volúmenes de Docker y vuelven al levantar otra vez con `docker compose up -d`. Para borrarlos y empezar de cero: `docker compose down -v`.

## Ejecutar la aplicación desde Windows (opcional)

Con solo las bases en Docker:

```powershell
docker compose up -d mysql-db mongo-db
cd nexusmarket
.\mvnw spring-boot:run
```

`application.properties` ya apunta a los puertos 3307 y 27018 del computador.
