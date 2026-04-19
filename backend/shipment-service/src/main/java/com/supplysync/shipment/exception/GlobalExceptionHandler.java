package com.supplysync.shipment.exception;

import com.supplysync.shipment.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShipmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ShipmentNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(InvalidShipmentStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidShipmentStateException ex) {
        return build(HttpStatus.CONFLICT, "Invalid State Transition", ex.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(InvalidRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .status(status.value()).error(error).message(message).timestamp(LocalDateTime.now()).build());
    }
}
