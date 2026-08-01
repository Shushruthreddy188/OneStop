package com.onestop.coupon.service;

import com.onestop.coupon.domain.Coupon;
import com.onestop.coupon.repo.CouponRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CouponServiceTest {
    @Test
    void percentageDiscountHonorsItsMaximumCap() {
        CouponRepository repo = mock(CouponRepository.class);
        Coupon coupon = new Coupon();
        coupon.setCode("SAVE20");
        coupon.setDiscountType(Coupon.PERCENT);
        coupon.setDiscountValue(new BigDecimal("20"));
        coupon.setMinOrderAmount(BigDecimal.ZERO);
        coupon.setMaxDiscount(new BigDecimal("50"));
        coupon.setActive(true);
        when(repo.findByCodeIgnoreCase("SAVE20")).thenReturn(Optional.of(coupon));

        var result = new CouponService(repo).validate(" SAVE20 ", new BigDecimal("1000"));

        assertThat(result.valid()).isTrue();
        assertThat(result.discountAmount()).isEqualByComparingTo("50.00");
    }
}
