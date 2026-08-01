package com.onestop.order.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.onestop.order.error.ApiExceptions.DependencyException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Takes payment for an order via the Payment Service (trusted internal call). */
@Component
public class PaymentClient {

    private final RestClient paymentRestClient;

    public PaymentClient(RestClient paymentRestClient) {
        this.paymentRestClient = paymentRestClient;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentResult(boolean success, String status, String providerRef, String message) {
    }

    public PaymentResult charge(Long orderId, Long customerId, BigDecimal amount,
                                String currency, String method) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderId", orderId);
        body.put("customerId", customerId);
        body.put("amount", amount);
        body.put("currency", currency);
        body.put("method", method);
        try {
            return paymentRestClient.post()
                    .uri("/internal/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(PaymentResult.class);
        } catch (Exception e) {
            throw new DependencyException("Payment processing failed: " + e.getMessage());
        }
    }
}
