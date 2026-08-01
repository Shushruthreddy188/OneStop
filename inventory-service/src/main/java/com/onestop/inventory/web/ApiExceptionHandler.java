package com.onestop.inventory.web;

import com.onestop.inventory.error.ApiExceptions.InsufficientStockException;
import com.onestop.inventory.error.ApiExceptions.NotFoundException;
import com.onestop.inventory.error.ApiExceptions.ReservationStateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = base(HttpStatus.BAD_REQUEST, "Validation failed");
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficient(InsufficientStockException ex) {
        Map<String, Object> body = base(HttpStatus.CONFLICT, "Insufficient stock");
        body.put("unavailableProductIds", ex.getUnavailableProductIds());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ReservationStateException.class)
    public ResponseEntity<Map<String, Object>> handleState(ReservationStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(base(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(base(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    private static Map<String, Object> base(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
