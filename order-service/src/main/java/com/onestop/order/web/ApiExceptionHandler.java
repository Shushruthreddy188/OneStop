package com.onestop.order.web;

import com.onestop.order.error.ApiExceptions.DependencyException;
import com.onestop.order.error.ApiExceptions.EmptyCartException;
import com.onestop.order.error.ApiExceptions.InsufficientStockException;
import com.onestop.order.error.ApiExceptions.NotFoundException;
import com.onestop.order.error.ApiExceptions.OrderStateException;
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
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fields.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        Map<String, Object> body = base(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<Map<String, Object>> handleEmptyCart(EmptyCartException ex) {
        return ResponseEntity.badRequest().body(base(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficient(InsufficientStockException ex) {
        Map<String, Object> body = base(HttpStatus.CONFLICT, "Insufficient stock");
        body.put("unavailableSkus", ex.getUnavailableSkus());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(OrderStateException.class)
    public ResponseEntity<Map<String, Object>> handleState(OrderStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(base(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(base(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(DependencyException.class)
    public ResponseEntity<Map<String, Object>> handleDependency(DependencyException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(base(HttpStatus.BAD_GATEWAY, ex.getMessage()));
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
