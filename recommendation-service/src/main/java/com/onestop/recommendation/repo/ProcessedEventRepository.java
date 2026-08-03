package com.onestop.recommendation.repo;

import com.onestop.recommendation.domain.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {

    /**
     * Claim an event id for processing. Returns 1 the first time an id is seen
     * and 0 on every redelivery, giving the consumer exactly-once side effects.
     */
    @Modifying
    @Query(value = """
            INSERT INTO processed_event (event_id, processed_at)
            VALUES (:eventId, now())
            ON CONFLICT (event_id) DO NOTHING
            """, nativeQuery = true)
    int claim(@Param("eventId") String eventId);
}
