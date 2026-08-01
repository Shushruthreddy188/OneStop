package com.onestop.coupon.repo;

import com.onestop.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    List<Coupon> findByActiveTrueOrderByMinOrderAmountAsc();
}
