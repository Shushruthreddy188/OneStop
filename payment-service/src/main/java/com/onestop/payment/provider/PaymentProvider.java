package com.onestop.payment.provider;

import java.math.BigDecimal;

/** A payment backend. The active one is selected by {@code onestop.payment.provider}. */
public interface PaymentProvider {

    String name();

    PaymentOutcome charge(ChargeCommand command);

    record ChargeCommand(Long orderId, Long customerId, BigDecimal amount, String currency, String method) {
    }

    record PaymentOutcome(boolean success, String status, String providerRef, String failureReason) {
    }
}
