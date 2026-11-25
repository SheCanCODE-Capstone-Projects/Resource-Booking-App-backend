package org.example.resourcebookingappbackend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 BAD REQUEST - DTO @Valid / @Pattern / @NotBlank failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing // Prevents crash if a field fails multiple rules
                ));

        Map<String, Object> body = buildErrorBody(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("errors", fieldErrors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 400 BAD REQUEST - Malformed JSON or invalid request body
    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
        return new ResponseEntity<>(
                buildErrorBody(HttpStatus.BAD_REQUEST, "Malformed request payload or invalid argument"),
                HttpStatus.BAD_REQUEST
        );
    }

    // 401 UNAUTHORIZED - Wrong password or failed authentication
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<Map<String, Object>> handleUnauthorized(Exception ex) {
        return new ResponseEntity<>(
                buildErrorBody(HttpStatus.UNAUTHORIZED, "Invalid email/username or password"),
                HttpStatus.UNAUTHORIZED
        );
    }

    // 404 NOT FOUND - User or requested resource does not exist
    @ExceptionHandler({UsernameNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return new ResponseEntity<>(
                buildErrorBody(HttpStatus.NOT_FOUND, ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    // 409 CONFLICT - Account/email/username already exists
    @ExceptionHandler(UserAlreadyExist.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExist(UserAlreadyExist ex) {
        return new ResponseEntity<>(
                buildErrorBody(HttpStatus.CONFLICT, ex.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    // 500 INTERNAL SERVER ERROR - Fallback for unexpected unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllUncaughtExceptions(Exception ex) {
        return new ResponseEntity<>(
                buildErrorBody(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private Map<String, Object> buildErrorBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}