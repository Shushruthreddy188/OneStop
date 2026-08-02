package com.onestop.activity.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ActivityPublisher {

    private static final Logger log = LoggerFactory.getLogger(ActivityPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public ActivityPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                             @Value("${onestop.kafka.topics.activity-events:activity.events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /** Key by session (or customer) so a user's events keep order. Best-effort. */
    public void publish(ActivityMessage message) {
        String key = message.sessionId() != null ? message.sessionId()
                : (message.customerId() != null ? String.valueOf(message.customerId()) : message.eventId());
        try {
            kafkaTemplate.send(topic, key, message);
        } catch (Exception e) {
            log.warn("Failed to publish activity {} (non-fatal): {}", message.eventId(), e.getMessage());
        }
    }
}
