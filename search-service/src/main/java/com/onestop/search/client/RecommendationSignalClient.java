package com.onestop.search.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pulls aggregated popularity signals from the recommendation-service to enrich
 * the search index. Best-effort: if the signals are unavailable the index is
 * still built (without behavioural boosting), so ranking degrades gracefully.
 */
@Component
public class RecommendationSignalClient {

    private static final Logger log = LoggerFactory.getLogger(RecommendationSignalClient.class);

    private final RestClient recommendationRestClient;

    public RecommendationSignalClient(RestClient recommendationRestClient) {
        this.recommendationRestClient = recommendationRestClient;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PopularitySignal(Long productId, Double score) {
    }

    /** Map of productId → popularity score; empty when the service is unreachable. */
    public Map<Long, Float> fetchPopularity() {
        try {
            List<PopularitySignal> signals = recommendationRestClient.get()
                    .uri("/internal/signals/popularity")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (signals == null) {
                return Map.of();
            }
            Map<Long, Float> out = new HashMap<>();
            for (PopularitySignal s : signals) {
                if (s.productId() != null && s.score() != null && s.score() > 0) {
                    out.put(s.productId(), s.score().floatValue());
                }
            }
            log.info("Fetched {} popularity signals for search ranking", out.size());
            return out;
        } catch (Exception e) {
            log.warn("Could not fetch popularity signals; indexing without behavioural boost: {}",
                    e.getMessage());
            return Map.of();
        }
    }
}
