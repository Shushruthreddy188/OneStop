package com.onestop.coupon.service;

import com.onestop.coupon.domain.Coupon;
import com.onestop.coupon.repo.CouponRepository;
import com.onestop.coupon.web.dto.CouponDtos.CouponDto;
import com.onestop.coupon.web.dto.CouponDtos.ValidationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CouponService {

    private final CouponRepository coupons;

    public CouponService(CouponRepository coupons) {
        this.coupons = coupons;
    }

    @Transactional(readOnly = true)
    public ValidationResult validate(String code, BigDecimal orderAmount) {
        Coupon coupon = coupons.findByCodeIgnoreCase(code.trim()).orElse(null);
        if (coupon == null) {
            return invalid(code, "Invalid coupon code");
        }
        if (!coupon.isActive()) {
            return invalid(code, "This coupon is no longer active");
        }
        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(OffsetDateTime.now())) {
            return invalid(code, "This coupon has expired");
        }
        if (orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            return invalid(code, "Requires a minimum order of " + coupon.getMinOrderAmount());
        }

        BigDecimal discount = computeDiscount(coupon, orderAmount);
        return new ValidationResult(true, coupon.getCode(), coupon.getDiscountType(),
                coupon.getDiscountValue(), discount, "Coupon applied");
    }

    @Transactional
    public CouponDto create(com.onestop.coupon.web.dto.CouponDtos.CreateCouponRequest req) {
        Coupon c = new Coupon();
        c.setCode(req.code().trim().toUpperCase());
        c.setDescription(req.description());
        c.setDiscountType(req.discountType().trim().toUpperCase());
        c.setDiscountValue(req.discountValue());
        c.setMinOrderAmount(req.minOrderAmount() != null ? req.minOrderAmount() : BigDecimal.ZERO);
        c.setMaxDiscount(req.maxDiscount());
        c.setActive(true);
        Coupon saved = coupons.save(c);
        return new CouponDto(saved.getCode(), saved.getDescription(), saved.getDiscountType(),
                saved.getDiscountValue(), saved.getMinOrderAmount());
    }

    @Transactional(readOnly = true)
    public List<CouponDto> listActive() {
        OffsetDateTime now = OffsetDateTime.now();
        return coupons.findByActiveTrueOrderByMinOrderAmountAsc().stream()
                .filter(c -> c.getExpiresAt() == null || c.getExpiresAt().isAfter(now))
                .map(c -> new CouponDto(c.getCode(), c.getDescription(), c.getDiscountType(),
                        c.getDiscountValue(), c.getMinOrderAmount()))
                .toList();
    }

    private static BigDecimal computeDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount;
        if (Coupon.PERCENT.equals(coupon.getDiscountType())) {
            discount = orderAmount.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        } else {
            discount = coupon.getDiscountValue();
        }
        // A discount can never exceed the order amount.
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    private static ValidationResult invalid(String code, String message) {
        return new ValidationResult(false, code, null, null, BigDecimal.ZERO, message);
    }
}
