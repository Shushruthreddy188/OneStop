package com.onestop.payment.repo;

import com.onestop.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderIdAndCustomerIdOrderByIdAsc(Long orderId, Long customerId);

    Optional<Payment> findByOrderId(Long orderId);

    @Query(value = "SELECT pg_advisory_xact_lock(:orderId)", nativeQuery = true)
    void lockOrder(@Param("orderId") Long orderId);
}
