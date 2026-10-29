# Global Exception Handler

## Descripción

`adapters/rest/exception/GlobalExceptionHandler` (`@RestControllerAdvice`) convierte cada excepción en una respuesta HTTP. El dominio lanza excepciones de Java normales y no sabe nada de HTTP; esta clase es el único lugar donde se decide el código de respuesta de los controladores.

Las peticiones que Spring Security detiene antes del controlador (sin token, token inválido o rol equivocado) las responde `SecurityErrorHandler` con el mismo cuerpo: 401 y 403.

Todas las respuestas de error tienen el mismo cuerpo (`ErrorResponseDTO`):

```json
{
  "timestamp": "2026-10-28T21:40:12.345",
  "status": 409,
  "error": "Conflict",
  "message": "Not enough stock of Camiseta.",
  "path": "/api/v1/buyer/cart/items"
}
```

---

## Correspondencia de excepciones

| Excepción | Código | Cuándo pasa |
|---|---|---|
| `InvalidCredentialsException` | 401 | Login con un correo que no existe o una contraseña equivocada. El mensaje es el mismo en los dos casos. |
| `AccessDeniedException` | 403 | El rol del usuario no puede hacer esa operación, o el recurso es de otro (por ejemplo, el producto de otro vendedor). |
| `EntityNotFoundException` | 404 | El id de la URL o del cuerpo no existe. |
| `IllegalArgumentException` | 400 | Dato inválido para el dominio: cantidad cero, dirección que no es del comprador, monto que no coincide. |
| `IllegalStateException` | 409 | Regla de negocio rechazada por el estado actual: sin stock, pedido ya pagado, correo repetido. |
| `MethodArgumentNotValidException` | 400 | El cuerpo no pasó `@Valid`. El mensaje dice qué campo falló. |
| `HttpMessageNotReadableException` | 400 | El JSON está mal escrito o un valor tiene el tipo equivocado (un enum que no existe, texto en un número). |
| `MethodArgumentTypeMismatchException` | 400 | Un parámetro de la URL no tiene el formato esperado (un id que no es UUID). |
| `NoResourceFoundException` | 404 | La ruta no existe. |
| `HttpRequestMethodNotSupportedException` | 405 | La ruta existe pero no con ese método (GET en vez de POST, por ejemplo). |
| `Exception` | 500 | Cualquier otro error. Se registra en el log de la aplicación. |

---

## Por qué 400 y 409

El dominio usa `IllegalArgumentException` cuando el dato en sí está mal, y `IllegalStateException` cuando el dato está bien pero el estado del negocio no lo permite. Por eso el primero responde 400 (la petición tiene un problema) y el segundo 409 (conflicto con el estado actual), igual que el proyecto de referencia separa la validación de las reglas de negocio.