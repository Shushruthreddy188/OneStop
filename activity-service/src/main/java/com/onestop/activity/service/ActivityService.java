package com.onestop.activity.service;

import com.onestop.activity.domain.ActivityEvent;
import com.onestop.activity.event.ActivityMessage;
import com.onestop.activity.event.ActivityPublisher;
import com.onestop.activity.repo.ActivityEventRepository;
import com.onestop.activity.web.dto.ActivityDtos.EventInput;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ActivityService {

    private final ActivityEventRepository repo;
    private final ActivityPublisher publisher;

    public ActivityService(ActivityEventRepository repo, ActivityPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    /**
     * Record one event idempotently (by eventId) and, only when newly stored,
     * publish it to the activity stream. Returns true if it was newly recorded.
     */
    @Transactional
    public boolean record(EventInput in, Long customerId, String correlationId) {
        if (repo.existsByEventId(in.eventId())) {
            return false;
        }
        ActivityEvent e = new ActivityEvent();
        e.setEventId(in.eventId());
        e.setEventType(in.eventType().trim().toUpperCase());
        e.setSessionId(in.sessionId());
        e.setCustomerId(customerId);
        e.setProductId(in.productId());
        e.setQuery(in.query());
        e.setPosition(in.position());
        e.setSource(in.source());
        e.setCorrelationId(correlationId);
        e.setSchemaVersion(1);
        OffsetDateTime occurredAt = in.occurredAt() != null ? in.occurredAt() : OffsetDateTime.now();
        e.setOccurredAt(occurredAt);

        try {
            repo.saveAndFlush(e);
        } catch (DataIntegrityViolationException duplicate) {
            // Concurrent duplicate with the same eventId — already recorded.
            return false;
        }

        publisher.publish(new ActivityMessage(e.getEventId(), e.getEventType(), e.getSessionId(),
                e.getCustomerId(), e.getProductId(), e.getQuery(), e.getPosition(), e.getSource(),
                e.getCorrelationId(), e.getSchemaVersion(), occurredAt));
        return true;
    }
}
