package com.onestop.coupon.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public final class CouponDtos {

    private CouponDtos() {
    }

    public record ValidateRequest(
            @NotBlank String code,
            @NotNull @PositiveOrZero BigDecimal orderAmount) {
    }

    /** Validation result. When valid, discountAmount is what to subtract from the order. */
    public record ValidationResult(
            boolean valid,
            String code,
            String discountType,
            BigDecimal discountValue,
            BigDecimal discountAmount,
            String message) {
    }

    public record CouponDto(
            String code,
            String description,
            String discountType,
            BigDecimal discountValue,
            BigDecimal minOrderAmount) {
    }

    public record CreateCouponRequest(
            @NotBlank String code,
            String description,
            @NotBlank String discountType,
            @NotNull @PositiveOrZero BigDecimal discountValue,
            BigDecimal minOrderAmount,
            BigDecimal maxDiscount) {
    }
}
