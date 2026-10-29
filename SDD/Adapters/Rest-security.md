# REST Security (JWT y CORS)

## 1. Descripción general

La seguridad sigue el proyecto de referencia: sesiones sin estado, un token JWT firmado que identifica al usuario en cada petición y cada ruta abierta solo al rol de su puerto de entrada. Las clases están en `adapters/security/`; el dominio no conoce ni Spring Security ni JWT.

```text
POST /api/v1/auth/login {email, password}
      |  PublicAccessPort.login -> LoginService
      |     UserRepositoryPort.findByEmail
      |     PasswordServicePort.matches   (BCryptPasswordServiceAdapter)
      |     TokenServicePort.generateToken (JwtProvider)
      v
{ token, tokenType: "Bearer", expiresInSeconds, user }

Siguientes peticiones: Authorization: Bearer <token>
      |  JwtAuthenticationFilter: firma, vencimiento y usuario activo
      |  JwtClaimsMapper: claims -> User del dominio (sin ir a la base de datos)
      |  SecurityConfig: el rol del token debe ser el de la ruta
      v
XxxRestController (@AuthenticationPrincipal User) -> puerto de entrada del rol
```

---

## 2. Clases

| Clase | Qué hace |
|---|---|
| `SecurityConfig` | Sesiones sin estado, CSRF desactivado (API sin cookies), CORS, reglas por ruta y el filtro JWT antes del de usuario y contraseña. Define el `PasswordEncoder` BCrypt. |
| `JwtProvider` | Implementa `TokenServicePort`. Firma el token con HS256 y `jwt.secret`, con vencimiento `jwt.expiration-ms` (una hora). `readUser` valida firma y vencimiento y devuelve el usuario, o nada si el token no sirve. |
| `JwtAuthenticationFilter` | Lee `Authorization: Bearer ...` en cada petición. Si el token es válido y el usuario está activo, lo deja autenticado; si no, la petición sigue como anónima y la regla de la ruta responde 401. |
| `mappers/JwtClaimsMapper` | User del dominio <-> claims del token. |
| `dtos/AuthenticatedUserPrincipal` | Envuelve el User para Spring Security. Su autoridad es `ROLE_` + rol, que es lo que revisa `hasRole`. Los controladores lo reciben con `@AuthenticationPrincipal(expression = "user")`. |
| `BCryptPasswordServiceAdapter` | Implementa `PasswordServicePort` con BCrypt. En la base de datos solo queda el hash (`users.password_hash`). |
| `SecurityErrorHandler` | Responde 401 y 403 con el mismo JSON que `GlobalExceptionHandler`. |

---

## 3. Reglas por ruta

| Ruta | Acceso |
|---|---|
| `/api/v1/auth/**` | público |
| `/api/v1/public/**` | público |
| `/error` | público |
| `/api/v1/buyer/**` | solo `BUYER` |
| `/api/v1/seller/**` | solo `SELLER` |
| `/api/v1/logistics/**` | solo `LOGISTIC_OPERATOR` |
| `/api/v1/admin/**` | solo `ADMIN` |
| `/api/v1/supervisor/**` | solo `SUPERVISOR` |
| cualquier otra | usuario autenticado |

Las reglas por ruta son la primera barrera. El dominio vuelve a revisar cada permiso con `AuthorizationService` y las validaciones de dueño (un vendedor no toca productos de otro aunque tenga el rol correcto).

---

## 4. Contenido del token

| Claim | Contenido |
|---|---|
| `sub` | id del usuario |
| `jti` | id único del token |
| `iat` / `exp` | emisión y vencimiento |
| `firstNames`, `lastNames`, `email`, `document` | datos del usuario |
| `role` | rol (`BUYER`, `SELLER`, `LOGISTIC_OPERATOR`, `ADMIN`, `SUPERVISOR`) |
| `status` | `ACTIVE` o `BLOCKED` |

La contraseña nunca va en el token. Con estos datos el filtro reconstruye el `User` en cada petición sin consultar la base de datos, como el proyecto de referencia. La consecuencia, la misma que acepta el ejemplo, es que un cambio de rol o un bloqueo se aplica cuando el usuario vuelve a iniciar sesión o cuando vence su token (una hora). El login sí rechaza de inmediato a un usuario bloqueado.

---

## 5. Configuración

| Propiedad | Variable de entorno | Valor por defecto |
|---|---|---|
| `jwt.secret` | `JWT_SECRET` | `TDEATDEATDEATDEATDEATDEATDEATDEA` (académico, público; cámbialo fuera del curso) |
| `jwt.expiration-ms` | `JWT_EXPIRATION_MS` | `3600000` (una hora) |
| origen CORS permitido | `FRONTEND_ORIGIN` | `http://localhost:5173` |
| contraseña del admin inicial | `ADMIN_PASSWORD` | `Admin12345*` |

Al arrancar, `BootstrapAdminRunner` crea el administrador inicial con esa contraseña, o se la asigna si el administrador existía antes de que hubiera contraseñas. Los usuarios creados antes de la Fase D no tienen contraseña y no pueden iniciar sesión; hay que registrarlos de nuevo.

---

## 6. Pruebas

* `LoginServiceTest`: contraseña correcta, equivocada, correo desconocido (mismo mensaje), usuario sin contraseña y usuario bloqueado.
* `JwtProviderTest`: firma y lectura real con jjwt, token alterado, token de otra clave y token vencido.
* `SecurityAdaptersTest`: claims sin contraseña, principal con `ROLE_`, BCrypt y el filtro con token válido, de usuario bloqueado, falso, sin `Bearer` y sin encabezado.
* `AuthRestControllerTest` y `BootstrapAdminRunnerTest`: login por la API y administrador inicial.