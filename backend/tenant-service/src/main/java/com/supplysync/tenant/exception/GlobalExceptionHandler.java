package com.supplysync.tenant.exception;

import com.supplysync.tenant.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler for the tenant-service.
 *
 * HOW IT WORKS:
 * - @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * - It intercepts exceptions thrown by ANY controller in this service
 * - Spring scans for @ExceptionHandler methods that match the thrown exception type
 * - The matching handler builds a clean JSON error response instead of a stack trace
 *
 * FLOW:
 * Controller throws TenantNotFoundException
 *   → Spring catches it
 *   → Finds handleTenantNotFound() because it has @ExceptionHandler(TenantNotFoundException.class)
 *   → Executes that method
 *   → Returns the ResponseEntity<ErrorResponse> as JSON to the client
 *
 * WITHOUT THIS: The client would see ugly whitelabel error pages or raw stack traces.
 * WITH THIS: The client gets a consistent, structured JSON error every time.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles: Tenant not found by ID or subdomain
     * Returns: 404 Not Found
     */
    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTenantNotFound(TenantNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())           // 404
                .error("Not Found")
                .message(ex.getMessage())                        // the message we passed when throwing
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        // ResponseEntity.status(404) sets the HTTP status code
        // .body(error) sets the JSON response body
    }

    /**
     * Handles: Duplicate tenant (same name or subdomain already exists)
     * Returns: 409 Conflict
     *
     * Why 409 and not 400? Because the request itself is valid — the problem is
     * that it conflicts with existing data. That's what 409 means.
     */
    @ExceptionHandler(TenantAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleTenantAlreadyExists(TenantAlreadyExistsException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())             // 409
                .error("Conflict")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handles: Bad input from the client (null fields, invalid values)
     * Returns: 400 Bad Request
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())          // 400
                .error("Bad Request")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles: IllegalArgumentException — this is thrown automatically by
     * Enum.valueOf() when you pass an invalid value like Plan.valueOf("GOLD").
     * Instead of letting it bubble up as a 500, we catch it and return 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())          // 400
                .error("Bad Request")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles: Everything else we didn't explicitly handle above.
     * This is the safety net — no exception should ever reach the client as a raw stack trace.
     * Returns: 500 Internal Server Error
     *
     * IMPORTANT: In production, you'd log the full exception here (ex.printStackTrace() or a logger)
     * but NEVER expose internal details to the client. The message is generic on purpose.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
