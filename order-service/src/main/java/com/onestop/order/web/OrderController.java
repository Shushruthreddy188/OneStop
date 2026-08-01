package com.onestop.order.web;

import com.onestop.order.security.AuthenticatedUser;
import com.onestop.order.service.OrderService;
import com.onestop.order.web.dto.OrderDtos.CheckoutRequest;
import com.onestop.order.web.dto.OrderDtos.OrderDto;
import com.onestop.order.web.dto.OrderDtos.OrderSummaryDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto placeOrder(@AuthenticationPrincipal AuthenticatedUser user,
                               @Valid @RequestBody CheckoutRequest request) {
        return orderService.placeOrder(user, request);
    }

    @GetMapping
    public List<OrderSummaryDto> listOrders(@AuthenticationPrincipal AuthenticatedUser user) {
        return orderService.listOrders(user.userId());
    }

    @GetMapping("/{orderId}")
    public OrderDto getOrder(@AuthenticationPrincipal AuthenticatedUser user,
                             @PathVariable Long orderId) {
        return orderService.getOrder(user.userId(), orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public OrderDto cancelOrder(@AuthenticationPrincipal AuthenticatedUser user,
                                @PathVariable Long orderId) {
        return orderService.cancelOrder(user.userId(), orderId);
    }
}
