package com.onestop.order.repo;

import com.onestop.order.domain.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=localhost:1",
        "spring.kafka.producer.properties.max.block.ms=1"
})
@Testcontainers
class OrderIdempotencyTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired OrderRepository orders;
    @Autowired PlatformTransactionManager transactionManager;

    @BeforeEach
    void clearOrders() {
        orders.deleteAll();
    }

    @Test
    void concurrentSameCustomerAndKeyCreatesExactlyOneOrder() throws Exception {
        int callers = 8;
        CountDownLatch ready = new CountDownLatch(callers);
        CountDownLatch start = new CountDownLatch(1);
        List<Callable<Boolean>> attempts = new ArrayList<>();

        for (int i = 0; i < callers; i++) {
            attempts.add(() -> {
                ready.countDown();
                start.await();
                try {
                    new TransactionTemplate(transactionManager).executeWithoutResult(
                            ignored -> orders.saveAndFlush(order(7L, "same-key")));
                    return true;
                } catch (DataIntegrityViolationException expected) {
                    return false;
                }
            });
        }

        try (var executor = Executors.newFixedThreadPool(callers)) {
            var futures = attempts.stream().map(executor::submit).toList();
            ready.await();
            start.countDown();

            long successes = 0;
            for (var future : futures) {
                if (future.get()) successes++;
            }
            assertThat(successes).isOne();
        }

        assertThat(orders.count()).isOne();
    }

    @Test
    void sameKeyCanBeUsedByDifferentCustomers() {
        orders.saveAndFlush(order(7L, "shared-key"));
        orders.saveAndFlush(order(8L, "shared-key"));

        assertThat(orders.count()).isEqualTo(2);
    }

    private static Order order(Long customerId, String key) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setStatus(Order.PENDING);
        order.setSubtotal(BigDecimal.ZERO);
        order.setTax(BigDecimal.ZERO);
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setTotal(BigDecimal.ZERO);
        order.setPaymentMethod("COD");
        order.setIdempotencyKey(key);
        return order;
    }
}
