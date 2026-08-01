package com.onestop.identity.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class PingController {

    @GetMapping("/api/auth/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "service", "identity-service",
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }
}
