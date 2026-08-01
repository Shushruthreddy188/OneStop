package com.onestop.notification.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=localhost:1",
        "spring.kafka.consumer.properties.request.timeout.ms=1000",
        "spring.kafka.consumer.properties.default.api.timeout.ms=1000"
})
@Testcontainers
class NotificationIdempotencyTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired NotificationLogRepository repository;
    @Autowired PlatformTransactionManager transactionManager;

    @BeforeEach
    void clearLog() {
        repository.deleteAll();
    }

    @Test
    void concurrentDuplicateEventsCreateOneKafkaNotification() throws Exception {
        int deliveries = 8;
        CountDownLatch ready = new CountDownLatch(deliveries);
        CountDownLatch start = new CountDownLatch(1);
        List<Callable<Integer>> attempts = new ArrayList<>();
        for (int i = 0; i < deliveries; i++) {
            attempts.add(() -> {
                ready.countDown();
                start.await();
                return new TransactionTemplate(transactionManager).execute(status ->
                        repository.insertKafkaConfirmationIfAbsent(
                                42L, "customer@example.com", "Order #42 confirmed", "Confirmed"));
            });
        }

        try (var executor = Executors.newFixedThreadPool(deliveries)) {
            var futures = attempts.stream().map(executor::submit).toList();
            ready.await();
            start.countDown();
            int inserts = 0;
            for (var future : futures) inserts += future.get();
            assertThat(inserts).isOne();
        }
        assertThat(repository.count()).isOne();
    }
}
