package com.onestop.order.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.onestop.order.error.ApiExceptions.DependencyException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

/** Validates coupon codes against the Coupon Service during checkout. */
@Component
public class CouponClient {

    private final RestClient couponRestClient;

    public CouponClient(RestClient couponRestClient) {
        this.couponRestClient = couponRestClient;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CouponValidation(boolean valid, BigDecimal discountAmount, String message) {
    }

    /** Re-validate the coupon at order time (source of truth) and get the discount. */
    public CouponValidation validate(String code, BigDecimal orderAmount) {
        try {
            return couponRestClient.post()
                    .uri("/api/coupons/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("code", code, "orderAmount", orderAmount))
                    .retrieve()
                    .body(CouponValidation.class);
        } catch (Exception e) {
            throw new DependencyException("Coupon validation failed: " + e.getMessage());
        }
    }
}
