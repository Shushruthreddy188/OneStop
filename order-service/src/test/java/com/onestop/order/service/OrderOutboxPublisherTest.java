package com.onestop.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onestop.order.domain.OrderOutboxEvent;
import com.onestop.order.event.OrderConfirmedEvent;
import com.onestop.order.event.OrderEventPublisher;
import com.onestop.order.repo.OrderOutboxRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderOutboxPublisherTest {

    @Test
    void marksAcknowledgedKafkaMessagePublished() throws Exception {
        OrderOutboxRepository repository = mock(OrderOutboxRepository.class);
        OrderEventPublisher publisher = mock(OrderEventPublisher.class);
        OrderOutboxStateStore stateStore = mock(OrderOutboxStateStore.class);
        ObjectMapper mapper = new ObjectMapper();
        OrderOutboxEvent entry = entry(mapper);
        when(repository.findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(eq("PENDING"), any()))
                .thenReturn(List.of(entry));
        when(publisher.publishOrderConfirmed(any())).thenReturn(true);

        new OrderOutboxPublisher(repository, publisher, stateStore, mapper).publishPending();

        verify(stateStore).markPublished(8L);
        verify(stateStore, never()).scheduleRetry(anyLong(), anyString());
    }

    @Test
    void schedulesRetryWhenKafkaDoesNotAcknowledge() throws Exception {
        OrderOutboxRepository repository = mock(OrderOutboxRepository.class);
        OrderEventPublisher publisher = mock(OrderEventPublisher.class);
        OrderOutboxStateStore stateStore = mock(OrderOutboxStateStore.class);
        ObjectMapper mapper = new ObjectMapper();
        OrderOutboxEvent entry = entry(mapper);
        when(repository.findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(eq("PENDING"), any()))
                .thenReturn(List.of(entry));
        when(publisher.publishOrderConfirmed(any())).thenReturn(false);

        new OrderOutboxPublisher(repository, publisher, stateStore, mapper).publishPending();

        verify(stateStore).scheduleRetry(8L, "Kafka publication failed");
        verify(stateStore, never()).markPublished(anyLong());
    }

    private static OrderOutboxEvent entry(ObjectMapper mapper) throws Exception {
        OrderOutboxEvent entry = new OrderOutboxEvent();
        entry.setId(8L);
        entry.setOrderId(42L);
        entry.setPayload(mapper.writeValueAsString(new OrderConfirmedEvent(
                42L, 7L, "customer@example.com", 2, new BigDecimal("25.00"), Instant.now().toString())));
        return entry;
    }
}
