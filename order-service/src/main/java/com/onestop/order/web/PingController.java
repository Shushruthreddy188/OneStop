package com.onestop.order.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class PingController {

    @GetMapping("/api/orders/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "service", "order-service",
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }
}
