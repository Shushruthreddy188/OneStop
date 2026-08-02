package com.onestop.activity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "activity_event")
@Getter
@Setter
@NoArgsConstructor
public class ActivityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "product_id")
    private Long productId;

    private String query;
    private Integer position;
    private String source;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "schema_version", nullable = false)
    private int schemaVersion = 1;

    @Column(name = "occurred_at")
    private OffsetDateTime occurredAt;
}
