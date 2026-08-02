package com.onestop.activity.event;

import com.onestop.activity.service.ActivityService;
import com.onestop.activity.web.dto.ActivityDtos.EventInput;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Turns confirmed orders into an ORDER_CONFIRMED activity event. Idempotent: the
 * eventId is derived from the order id, so redelivered events are deduplicated.
 */
@Component
public class OrderConfirmedListener {

    private final ActivityService activityService;

    public OrderConfirmedListener(ActivityService activityService) {
        this.activityService = activityService;
    }

    @KafkaListener(
            topics = "${onestop.kafka.topics.order-confirmed:order.confirmed}",
            groupId = "${spring.kafka.consumer.group-id:activity-service}")
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        EventInput input = new EventInput(
                "order-confirmed:" + event.orderId(),
                "ORDER_CONFIRMED",
                null, null, null, null,
                "order-service",
                null);
        activityService.record(input, event.customerId(), "order:" + event.orderId());
    }
}
