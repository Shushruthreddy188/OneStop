package com.onestop.recommendation.web;

import com.onestop.recommendation.domain.ProductSignal;
import com.onestop.recommendation.repo.ProductSignalRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes aggregated behavioural signals for other services to consume.
 *
 * <p>This is the "close the loop" hand-off: only validated aggregates (a
 * per-product popularity score derived from many view events) leave this
 * service — never raw activity events. Mounted under {@code /internal} and not
 * routed by the gateway, so it stays service-to-service within the network.
 */
@RestController
@RequestMapping("/internal/signals")
public class InternalSignalController {

    private final ProductSignalRepository productSignals;

    public InternalSignalController(ProductSignalRepository productSignals) {
        this.productSignals = productSignals;
    }

    /** Aggregated popularity per product. score = accumulated view count. */
    public record PopularitySignal(Long productId, double score) {
    }

    @GetMapping("/popularity")
    public List<PopularitySignal> popularity() {
        return productSignals.findAll().stream()
                .filter(s -> s.getViewCount() > 0)
                .map(s -> new PopularitySignal(s.getProductId(), (double) s.getViewCount()))
                .toList();
    }
}
