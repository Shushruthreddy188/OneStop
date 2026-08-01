package com.onestop.notification.repo;

import com.onestop.notification.domain.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    @Modifying
    @Query(value = """
            INSERT INTO notification_log (order_id, channel, recipient, subject, body, status)
            VALUES (:orderId, 'KAFKA', :recipient, :subject, :body, 'SENT')
            ON CONFLICT (order_id) WHERE channel = 'KAFKA' AND order_id IS NOT NULL
            DO NOTHING
            """, nativeQuery = true)
    int insertKafkaConfirmationIfAbsent(@Param("orderId") Long orderId,
                                        @Param("recipient") String recipient,
                                        @Param("subject") String subject,
                                        @Param("body") String body);
}
