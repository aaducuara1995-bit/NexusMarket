package application.adapters.rest.exception;

import application.adapters.rest.dtos.responses.ErrorResponseDTO;
import application.domain.exceptions.AccessDeniedException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Translates exceptions into HTTP answers with the same body (ErrorResponseDTO).
 * The domain throws plain Java exceptions; this is the only place that knows HTTP:
 * wrong credentials 401, not found 404, not allowed 403, invalid data 400, business rule
 * rejected 409. Requests without a valid token never get here: SecurityErrorHandler
 * answers them with the same body.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Wrong email or password at login. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return body(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return body(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** Invalid data sent by the client: a negative quantity, an empty name, a foreign address. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidData(IllegalArgumentException ex, HttpServletRequest request) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /** Valid data that the current state rejects: not enough stock, an order already paid. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessRule(IllegalStateException ex, HttpServletRequest request) {
        return body(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return body(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleMalformedBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return body(HttpStatus.BAD_REQUEST,
                "The request body is not valid JSON or has a value of the wrong type (check enums and numbers).", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadParameter(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return body(HttpStatus.BAD_REQUEST, "The parameter '" + ex.getName() + "' has an invalid format.", request);
    }

    /** A URL that no controller answers (a typo in the path, for example). */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnknownRoute(NoResourceFoundException ex, HttpServletRequest request) {
        return body(HttpStatus.NOT_FOUND, "There is no endpoint " + request.getMethod() + " "
                + request.getRequestURI() + ". Check the path in Api-rest-endpoints.md.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleWrongMethod(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return body(HttpStatus.METHOD_NOT_ALLOWED, "The path exists but does not accept " + ex.getMethod() + ".", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error. Check the application log.", request);
    }

    private static ResponseEntity<ErrorResponseDTO> body(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponseDTO(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), message, request.getRequestURI()));
    }
}