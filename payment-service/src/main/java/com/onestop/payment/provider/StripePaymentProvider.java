package com.onestop.payment.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stripe (test-mode) scaffold. Activated with {@code onestop.payment.provider=stripe}
 * and a {@code STRIPE_SECRET_KEY} (an sk_test_… sandbox key you supply — it is read
 * from the environment and never committed).
 *
 * <p>A real Stripe charge is not a single synchronous server call: the standard flow is
 * <ol>
 *   <li>server creates a PaymentIntent (amount, currency) → returns a client_secret,</li>
 *   <li>the browser confirms it with Stripe.js using the customer's card (card data
 *       never touches our servers),</li>
 *   <li>Stripe calls a webhook, and we mark the payment SUCCEEDED on
 *       {@code payment_intent.succeeded}.</li>
 * </ol>
 * Because that requires the frontend + webhook wiring (and a real key to exercise),
 * this provider is intentionally a documented placeholder rather than an untested,
 * pretend-working implementation. The {@link SimulatedPaymentProvider} is the default.
 */
@Component
@ConditionalOnProperty(name = "onestop.payment.provider", havingValue = "stripe")
public class StripePaymentProvider implements PaymentProvider {

    private final String secretKey;

    public StripePaymentProvider(@Value("${STRIPE_SECRET_KEY:}") String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String name() {
        return "STRIPE";
    }

    @Override
    public PaymentOutcome charge(ChargeCommand command) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "STRIPE_SECRET_KEY is not set. Provide an sk_test_… key to use the Stripe provider.");
        }
        throw new UnsupportedOperationException(
                "Stripe integration is a scaffold. Implement the PaymentIntent + Stripe.js + webhook flow, "
                        + "or keep onestop.payment.provider=simulated for local/dev.");
    }
}
