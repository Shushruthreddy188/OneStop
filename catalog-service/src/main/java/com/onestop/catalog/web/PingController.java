package com.onestop.catalog.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Minimal liveness/routing probe. Lets you confirm the service is up and that
 * the API Gateway routes reach it, before any real endpoints exist.
 */
@RestController
public class PingController {

    @GetMapping("/api/products/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "service", "catalog-service",
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }
}
