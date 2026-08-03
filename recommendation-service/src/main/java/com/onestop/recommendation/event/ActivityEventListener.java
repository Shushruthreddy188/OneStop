package com.onestop.recommendation.event;

import com.onestop.recommendation.repo.CustomerRecentViewRepository;
import com.onestop.recommendation.repo.ProcessedEventRepository;
import com.onestop.recommendation.repo.ProductSignalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Turns the raw activity stream into aggregated recommendation signals.
 * Idempotent: each activity event is applied at most once (see processed_event),
 * so Kafka at-least-once redelivery never inflates the counters.
 */
@Component
public class ActivityEventListener {

    private static final Logger log = LoggerFactory.getLogger(ActivityEventListener.class);

    private final ProcessedEventRepository processedEvents;
    private final ProductSignalRepository productSignals;
    private final CustomerRecentViewRepository recentViews;

    public ActivityEventListener(ProcessedEventRepository processedEvents,
                                 ProductSignalRepository productSignals,
                                 CustomerRecentViewRepository recentViews) {
        this.processedEvents = processedEvents;
        this.productSignals = productSignals;
        this.recentViews = recentViews;
    }

    @KafkaListener(topics = "${onestop.kafka.topics.activity-events}", groupId = "recommendation-service")
    @Transactional
    public void onActivity(ActivityEventMessage event) {
        if (event == null || event.eventId() == null) {
            return;
        }
        // Claim the event; a second delivery of the same id is a no-op.
        if (processedEvents.claim(event.eventId()) == 0) {
            return;
        }

        if ("PRODUCT_VIEWED".equals(event.eventType()) && event.productId() != null) {
            productSignals.incrementView(event.productId());
            if (event.customerId() != null) {
                recentViews.touch(event.customerId(), event.productId());
            }
            log.debug("Applied PRODUCT_VIEWED signal for product {}", event.productId());
        }
    }
}
