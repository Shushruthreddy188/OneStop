package com.onestop.payment.service;

import com.onestop.payment.domain.Payment;
import com.onestop.payment.provider.PaymentProvider;
import com.onestop.payment.provider.PaymentProvider.ChargeCommand;
import com.onestop.payment.provider.PaymentProvider.PaymentOutcome;
import com.onestop.payment.repo.PaymentRepository;
import com.onestop.payment.web.dto.PaymentDtos.PaymentDto;
import com.onestop.payment.web.dto.PaymentDtos.PaymentResult;
import com.onestop.payment.web.dto.PaymentDtos.ProcessPaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository payments;
    private final PaymentProvider provider;

    public PaymentService(PaymentRepository payments, PaymentProvider provider) {
        this.payments = payments;
        this.provider = provider;
    }

    @Transactional
    public PaymentResult process(ProcessPaymentRequest req) {
        String currency = (req.currency() == null || req.currency().isBlank()) ? "INR" : req.currency();
        String method = req.method().trim().toUpperCase();

        // Serialize attempts for the same business order across all service instances.
        // This prevents retries or concurrent requests from charging twice.
        payments.lockOrder(req.orderId());
        Payment existing = payments.findByOrderId(req.orderId()).orElse(null);
        if (existing != null) {
            return toResult(existing);
        }

        PaymentOutcome outcome = provider.charge(
                new ChargeCommand(req.orderId(), req.customerId(), req.amount(), currency, method));

        Payment payment = new Payment();
        payment.setOrderId(req.orderId());
        payment.setCustomerId(req.customerId());
        payment.setAmount(req.amount());
        payment.setCurrency(currency);
        payment.setMethod(method);
        payment.setStatus(outcome.status());
        payment.setProvider(provider.name());
        payment.setProviderRef(outcome.providerRef());
        payment.setFailureReason(outcome.failureReason());
        payments.save(payment);

        log.info("Payment {} for order {} via {} -> {}", payment.getId(), req.orderId(),
                provider.name(), outcome.status());

        return new PaymentResult(payment.getId(), outcome.success(), outcome.status(),
                provider.name(), outcome.providerRef(),
                outcome.success() ? "Payment " + outcome.status().toLowerCase() : outcome.failureReason());
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> listByOrder(Long customerId, Long orderId) {
        return payments.findByOrderIdAndCustomerIdOrderByIdAsc(orderId, customerId).stream()
                .map(p -> new PaymentDto(p.getId(), p.getOrderId(), p.getAmount(), p.getCurrency(),
                        p.getMethod(), p.getStatus(), p.getProvider(), p.getProviderRef(), p.getCreatedAt()))
                .toList();
    }

    private static PaymentResult toResult(Payment payment) {
        boolean success = !Payment.FAILED.equals(payment.getStatus());
        return new PaymentResult(payment.getId(), success, payment.getStatus(),
                payment.getProvider(), payment.getProviderRef(),
                success ? "Payment " + payment.getStatus().toLowerCase() : payment.getFailureReason());
    }
}
