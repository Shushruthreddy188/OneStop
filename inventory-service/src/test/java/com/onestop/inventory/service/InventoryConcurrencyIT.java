package com.onestop.inventory.service;

import com.onestop.inventory.domain.Inventory;
import com.onestop.inventory.error.ApiExceptions.InsufficientStockException;
import com.onestop.inventory.repo.InventoryRepository;
import com.onestop.inventory.repo.ReservationItemRepository;
import com.onestop.inventory.repo.ReservationRepository;
import com.onestop.inventory.web.dto.InventoryDtos.ReservationLine;
import com.onestop.inventory.web.dto.InventoryDtos.ReserveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class InventoryConcurrencyTest {

    private static final long PRODUCT_ID = 9001L;

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired InventoryService service;
    @Autowired InventoryRepository inventory;
    @Autowired ReservationRepository reservations;
    @Autowired ReservationItemRepository reservationItems;

    @BeforeEach
    void seedStock() {
        reservationItems.deleteAll();
        reservations.deleteAll();
        inventory.deleteAll();

        Inventory stock = new Inventory();
        stock.setProductId(PRODUCT_ID);
        stock.setAvailableQuantity(5);
        stock.setReservedQuantity(0);
        inventory.saveAndFlush(stock);
    }

    @Test
    void concurrentReservationsNeverOversell() throws Exception {
        int callers = 10;
        CountDownLatch ready = new CountDownLatch(callers);
        CountDownLatch start = new CountDownLatch(1);
        List<Callable<Boolean>> attempts = new ArrayList<>();

        for (int i = 0; i < callers; i++) {
            long orderId = i + 1L;
            attempts.add(() -> {
                ready.countDown();
                start.await();
                try {
                    service.reserve(new ReserveRequest(orderId,
                            List.of(new ReservationLine(PRODUCT_ID, 1))));
                    return true;
                } catch (InsufficientStockException expected) {
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
            assertThat(successes).isEqualTo(5);
        }

        Inventory remaining = inventory.findByProductId(PRODUCT_ID).orElseThrow();
        assertThat(remaining.getAvailableQuantity()).isZero();
        assertThat(remaining.getReservedQuantity()).isEqualTo(5);
        assertThat(reservations.count()).isEqualTo(5);
    }
}
