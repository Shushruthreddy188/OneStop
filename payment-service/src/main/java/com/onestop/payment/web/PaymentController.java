package com.onestop.payment.web;

import com.onestop.payment.service.PaymentService;
import com.onestop.payment.web.dto.PaymentDtos.PaymentDto;
import com.onestop.payment.web.dto.PaymentDtos.PaymentResult;
import com.onestop.payment.web.dto.PaymentDtos.ProcessPaymentRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** POST /internal/payments — trusted service-to-service charge (order-service). */
    @PostMapping("/internal/payments")
    public PaymentResult process(@Valid @RequestBody ProcessPaymentRequest request) {
        return paymentService.process(request);
    }

    /** GET /api/payments/order/{orderId} — the caller's payments for an order. */
    @GetMapping("/api/payments/order/{orderId}")
    public List<PaymentDto> byOrder(@AuthenticationPrincipal Long customerId,
                                    @PathVariable Long orderId) {
        return paymentService.listByOrder(customerId, orderId);
    }
}
