package com.onestop.coupon.web;

import com.onestop.coupon.service.CouponService;
import com.onestop.coupon.web.dto.CouponDtos.CouponDto;
import com.onestop.coupon.web.dto.CouponDtos.ValidateRequest;
import com.onestop.coupon.web.dto.CouponDtos.ValidationResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    /** POST /api/coupons/validate — check a code against an order amount and compute the discount. */
    @PostMapping("/validate")
    public ValidationResult validate(@Valid @RequestBody ValidateRequest request) {
        return couponService.validate(request.code(), request.orderAmount());
    }

    /** GET /api/coupons — available offers (active, non-expired). */
    @GetMapping
    public List<CouponDto> list() {
        return couponService.listActive();
    }
}
