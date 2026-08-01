package com.onestop.admin.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.onestop.admin.web.dto.AdminDtos.CouponSummaryDto;
import com.onestop.admin.web.dto.AdminDtos.CreateCouponRequest;
import com.onestop.admin.web.dto.AdminDtos.DashboardDto;
import com.onestop.admin.web.dto.AdminDtos.RecentOrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

/** Aggregates a read-only admin dashboard from the other services (no own DB). */
@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final RestClient catalogRestClient;
    private final RestClient orderRestClient;
    private final RestClient couponRestClient;

    public AdminService(RestClient catalogRestClient, RestClient orderRestClient, RestClient couponRestClient) {
        this.catalogRestClient = catalogRestClient;
        this.orderRestClient = orderRestClient;
        this.couponRestClient = couponRestClient;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PageMeta(long totalElements) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OrderSummary(long orderCount, long confirmedCount, BigDecimal revenue,
                                List<RecentOrderDto> recent) {
    }

    public DashboardDto dashboard() {
        long productCount = safe(() -> {
            PageMeta page = catalogRestClient.get()
                    .uri(u -> u.path("/api/products").queryParam("size", 1).build())
                    .retrieve().body(PageMeta.class);
            return page != null ? page.totalElements() : 0L;
        });

        long categoryCount = safe(() -> {
            List<Object> categories = catalogRestClient.get().uri("/api/categories")
                    .retrieve().body(new ParameterizedTypeReference<List<Object>>() {
                    });
            return categories != null ? (long) categories.size() : 0L;
        });

        List<CouponSummaryDto> coupons = listCoupons();
        OrderSummary orders = safeSummary();

        return new DashboardDto(productCount, categoryCount, coupons.size(),
                orders.orderCount(), orders.confirmedCount(), orders.revenue(), orders.recent());
    }

    public List<CouponSummaryDto> listCoupons() {
        try {
            List<CouponSummaryDto> list = couponRestClient.get().uri("/api/coupons")
                    .retrieve().body(new ParameterizedTypeReference<List<CouponSummaryDto>>() {
                    });
            return list != null ? list : List.of();
        } catch (Exception e) {
            log.warn("Coupon list failed: {}", e.getMessage());
            return List.of();
        }
    }

    public CouponSummaryDto createCoupon(CreateCouponRequest request) {
        return couponRestClient.post().uri("/internal/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve().body(CouponSummaryDto.class);
    }

    private OrderSummary safeSummary() {
        try {
            OrderSummary s = orderRestClient.get().uri("/internal/admin/summary")
                    .retrieve().body(OrderSummary.class);
            return s != null ? s : new OrderSummary(0, 0, BigDecimal.ZERO, List.of());
        } catch (Exception e) {
            log.warn("Order summary failed: {}", e.getMessage());
            return new OrderSummary(0, 0, BigDecimal.ZERO, List.of());
        }
    }

    private static long safe(java.util.function.Supplier<Long> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("Admin aggregate call failed: {}", e.getMessage());
            return 0L;
        }
    }
}
