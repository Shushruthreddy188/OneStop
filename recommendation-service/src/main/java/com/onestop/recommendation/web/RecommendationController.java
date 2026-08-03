package com.onestop.recommendation.web;

import com.onestop.recommendation.service.RecommendationService;
import com.onestop.recommendation.web.dto.RecommendationDtos.RecommendationRow;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recommendation surfaces. All endpoints are open; when a valid customer token
 * is present the results are personalized, otherwise a cold-start row is served.
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private static final int MAX_LIMIT = 24;

    private final RecommendationService recommendations;

    public RecommendationController(RecommendationService recommendations) {
        this.recommendations = recommendations;
    }

    @GetMapping("/trending")
    public RecommendationRow trending(@RequestParam(defaultValue = "8") int limit) {
        return recommendations.trending(clamp(limit));
    }

    @GetMapping("/recently-viewed")
    public RecommendationRow recentlyViewed(@AuthenticationPrincipal Long customerId,
                                            @RequestParam(defaultValue = "8") int limit) {
        return recommendations.recentlyViewed(customerId, clamp(limit));
    }

    @GetMapping("/for-you")
    public RecommendationRow forYou(@AuthenticationPrincipal Long customerId,
                                    @RequestParam(defaultValue = "8") int limit) {
        return recommendations.forYou(customerId, clamp(limit));
    }

    private static int clamp(int limit) {
        if (limit < 1) {
            return 1;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
