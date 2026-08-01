package com.onestop.notification.web;

import com.onestop.notification.domain.NotificationLog;
import com.onestop.notification.repo.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal endpoint that "sends" a notification. MVP simulates delivery by
 * logging to the console and recording it. In Milestone 6 this moves to a Kafka
 * consumer so order success no longer depends on notification delivery.
 */
@RestController
@RequestMapping("/internal/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationLogRepository repository;

    public NotificationController(NotificationLogRepository repository) {
        this.repository = repository;
    }

    public record NotificationRequest(Long orderId, String recipient, String subject, String body) {
    }

    @GetMapping("/orders/{orderId}")
    @Transactional(readOnly = true)
    public List<NotificationLog> findByOrder(@PathVariable Long orderId) {
        return repository.findByOrderIdOrderByIdAsc(orderId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    public void send(@RequestBody NotificationRequest request) {
        log.info("[NOTIFICATION] order={} to={} subject={}",
                request.orderId(), request.recipient(), request.subject());

        NotificationLog entry = new NotificationLog();
        entry.setOrderId(request.orderId());
        entry.setRecipient(request.recipient());
        entry.setSubject(request.subject());
        entry.setBody(request.body());
        entry.setChannel("CONSOLE");
        entry.setStatus("SENT");
        repository.save(entry);
    }
}
