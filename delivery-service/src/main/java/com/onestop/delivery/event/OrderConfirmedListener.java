package com.onestop.delivery.event;

import com.onestop.delivery.service.DeliveryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Creates a shipment when an order is confirmed (independent of checkout). */
@Component
public class OrderConfirmedListener {

    private final DeliveryService deliveryService;

    public OrderConfirmedListener(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @KafkaListener(
            topics = "${onestop.kafka.topics.order-confirmed:order.confirmed}",
            groupId = "${spring.kafka.consumer.group-id:delivery-service}")
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        deliveryService.createFromOrder(event.orderId(), event.customerId());
    }
}
