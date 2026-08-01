package com.onestop.order.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestClient notificationRestClient;

    public NotificationClient(RestClient notificationRestClient) {
        this.notificationRestClient = notificationRestClient;
    }

    public record NotificationRequest(Long orderId, String recipient, String subject, String body) {
    }

    /**
     * Best-effort: order success must NOT depend on notification delivery, so a
     * failure here is logged and swallowed (retried later / moved to Kafka in M6).
     */
    public void sendOrderConfirmation(Long orderId, String recipient, String subject, String body) {
        try {
            notificationRestClient.post()
                    .uri("/internal/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new NotificationRequest(orderId, recipient, subject, body))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Notification for order {} failed (non-fatal): {}", orderId, e.getMessage());
        }
    }
}
