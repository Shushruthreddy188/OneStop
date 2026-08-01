package com.onestop.notification.event;

import com.onestop.notification.repo.NotificationLogRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

class OrderConfirmedListenerTest {

    @Test
    void duplicateDeliveryUsesTheSameAtomicIdempotentInsert() {
        NotificationLogRepository repository = mock(NotificationLogRepository.class);
        when(repository.insertKafkaConfirmationIfAbsent(anyLong(), any(), anyString(), anyString()))
                .thenReturn(1, 0);
        OrderConfirmedListener listener = new OrderConfirmedListener(repository);
        OrderConfirmedEvent event = new OrderConfirmedEvent(
                42L, 7L, "customer@example.com", 2, new BigDecimal("25.00"), "2026-07-31T00:00:00Z");

        listener.onOrderConfirmed(event);
        listener.onOrderConfirmed(event);

        verify(repository, times(2)).insertKafkaConfirmationIfAbsent(
                eq(42L), eq("customer@example.com"),
                eq("Order #42 confirmed"),
                eq("Thanks! Your order of 2 item(s) totalling 25.00 is confirmed."));
        verify(repository, never()).save(any());
    }
}
