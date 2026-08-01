package com.onestop.delivery.repo;

import com.onestop.delivery.domain.Shipment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    boolean existsByOrderId(Long orderId);

    @EntityGraph(attributePaths = "events")
    Optional<Shipment> findByOrderIdAndCustomerId(Long orderId, Long customerId);

    /** Shipments not yet delivered, for the progress simulator. */
    List<Shipment> findByStatusNot(String status);
}
