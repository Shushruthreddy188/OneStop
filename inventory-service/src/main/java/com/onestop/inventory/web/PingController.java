package com.onestop.inventory.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class PingController {

    @GetMapping("/internal/inventory/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "service", "inventory-service",
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }
}
