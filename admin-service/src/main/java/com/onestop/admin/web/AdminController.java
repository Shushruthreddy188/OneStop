package com.onestop.admin.web;

import com.onestop.admin.service.AdminService;
import com.onestop.admin.web.dto.AdminDtos.CouponSummaryDto;
import com.onestop.admin.web.dto.AdminDtos.CreateCouponRequest;
import com.onestop.admin.web.dto.AdminDtos.DashboardDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Admin API. All of /api/admin/** requires ROLE_ADMIN (enforced in SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard() {
        return adminService.dashboard();
    }

    @GetMapping("/coupons")
    public List<CouponSummaryDto> coupons() {
        return adminService.listCoupons();
    }

    @PostMapping("/coupons")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponSummaryDto createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        return adminService.createCoupon(request);
    }
}
