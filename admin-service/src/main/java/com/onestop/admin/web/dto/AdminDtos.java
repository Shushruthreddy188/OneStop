package com.onestop.admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record RecentOrderDto(Long id, Long customerId, String status, BigDecimal total, String paymentMethod) {
    }

    public record DashboardDto(
            long productCount,
            long categoryCount,
            long couponCount,
            long orderCount,
            long confirmedOrderCount,
            BigDecimal revenue,
            List<RecentOrderDto> recentOrders) {
    }

    public record CouponSummaryDto(
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
