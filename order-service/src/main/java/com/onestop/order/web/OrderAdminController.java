package com.onestop.order.web;

import com.onestop.order.domain.Order;
import com.onestop.order.repo.OrderRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Internal admin summary (not gateway-exposed). Called by the Admin Service,
 * which enforces ROLE_ADMIN before reaching here.
 */
@RestController
@RequestMapping("/internal/admin")
public class OrderAdminController {

    private final OrderRepository orders;

    public OrderAdminController(OrderRepository orders) {
        this.orders = orders;
    }

    public record RecentOrder(Long id, Long customerId, String status, BigDecimal total, String paymentMethod) {
    }

    public record OrderSummary(long orderCount, long confirmedCount, BigDecimal revenue, List<RecentOrder> recent) {
    }

    @GetMapping("/summary")
    public OrderSummary summary() {
        List<RecentOrder> recent = orders.findTop10ByOrderByIdDesc().stream()
                .map(o -> new RecentOrder(o.getId(), o.getCustomerId(), o.getStatus(),
                        o.getTotal(), o.getPaymentMethod()))
                .toList();
        return new OrderSummary(orders.count(), orders.countByStatus(Order.CONFIRMED),
                orders.totalConfirmedRevenue(), recent);
    }
}
