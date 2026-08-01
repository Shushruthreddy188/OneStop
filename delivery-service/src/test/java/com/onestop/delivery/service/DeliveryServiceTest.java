package com.onestop.delivery.service;

import com.onestop.delivery.domain.Shipment;
import com.onestop.delivery.repo.ShipmentRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DeliveryServiceTest {
    @Test
    void activeShipmentAdvancesExactlyOneStage() {
        ShipmentRepository repo = mock(ShipmentRepository.class);
        Shipment shipment = new Shipment();
        shipment.setOrderId(3L); shipment.setCustomerId(4L);
        shipment.addEvent("CONFIRMED", "ready");
        when(repo.findByStatusNot("DELIVERED")).thenReturn(List.of(shipment));

        int advanced = new DeliveryService(repo).advanceActiveShipments();

        assertThat(advanced).isOne();
        assertThat(shipment.getStatus()).isEqualTo("PACKED");
        assertThat(shipment.getEvents()).hasSize(2);
    }
}
