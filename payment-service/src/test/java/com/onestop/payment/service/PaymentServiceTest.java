package com.onestop.payment.service;

import com.onestop.payment.domain.Payment;
import com.onestop.payment.provider.PaymentProvider;
import com.onestop.payment.repo.PaymentRepository;
import com.onestop.payment.web.dto.PaymentDtos.ProcessPaymentRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
    @Test
    void retryReturnsStoredResultWithoutChargingProviderAgain() {
        PaymentRepository repo = mock(PaymentRepository.class);
        PaymentProvider provider = mock(PaymentProvider.class);
        Payment stored = new Payment();
        stored.setId(12L); stored.setOrderId(40L); stored.setStatus("SUCCESS");
        stored.setProvider("simulated"); stored.setProviderRef("sim-order-40");
        when(repo.findByOrderId(40L)).thenReturn(Optional.of(stored));

        var result = new PaymentService(repo, provider).process(
                new ProcessPaymentRequest(40L, 2L, new BigDecimal("100"), "INR", "CARD"));

        assertThat(result.paymentId()).isEqualTo(12L);
        assertThat(result.success()).isTrue();
        verify(repo).lockOrder(40L);
        verifyNoInteractions(provider);
        verify(repo, never()).save(any());
    }
}
