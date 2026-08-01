package com.onestop.payment.provider;

import com.onestop.payment.domain.Payment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Default provider — no external calls, no real money. Cash-on-delivery is
 * accepted as pending; simulated card payments succeed, except a test amount
 * ending in .13 which is "declined" so the failure path can be exercised.
 */
@Component
@ConditionalOnProperty(name = "onestop.payment.provider", havingValue = "simulated", matchIfMissing = true)
public class SimulatedPaymentProvider implements PaymentProvider {

    private static final BigDecimal DECLINE_CENTS = new BigDecimal("0.13");

    @Override
    public String name() {
        return "SIMULATED";
    }

    @Override
    public PaymentOutcome charge(ChargeCommand cmd) {
        if ("COD".equalsIgnoreCase(cmd.method())) {
            // Nothing to collect now; the order is authorized, cash due on delivery.
            return new PaymentOutcome(true, Payment.PENDING, "cod-" + cmd.orderId(), null);
        }
        boolean declined = cmd.amount().remainder(BigDecimal.ONE).compareTo(DECLINE_CENTS) == 0;
        if (declined) {
            return new PaymentOutcome(false, Payment.FAILED, null, "Card declined (test)");
        }
        return new PaymentOutcome(true, Payment.SUCCEEDED, "sim-order-" + cmd.orderId(), null);
    }
}
