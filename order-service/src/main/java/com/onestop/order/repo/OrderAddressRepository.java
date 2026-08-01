package com.onestop.order.repo;

import com.onestop.order.domain.OrderAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {
}
