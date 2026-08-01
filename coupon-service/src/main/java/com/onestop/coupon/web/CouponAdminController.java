package com.onestop.coupon.web;

import com.onestop.coupon.service.CouponService;
import com.onestop.coupon.web.dto.CouponDtos.CouponDto;
import com.onestop.coupon.web.dto.CouponDtos.CreateCouponRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Internal coupon management (not gateway-exposed); called by the Admin Service. */
@RestController
@RequestMapping("/internal/coupons")
public class CouponAdminController {

    private final CouponService couponService;

    public CouponAdminController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponDto create(@Valid @RequestBody CreateCouponRequest request) {
        return couponService.create(request);
    }
}
