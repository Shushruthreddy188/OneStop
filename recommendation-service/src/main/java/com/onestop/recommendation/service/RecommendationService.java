package com.onestop.recommendation.service;

import com.onestop.recommendation.client.CatalogClient;
import com.onestop.recommendation.domain.ProductSignal;
import com.onestop.recommendation.repo.CustomerRecentViewRepository;
import com.onestop.recommendation.repo.ProductSignalRepository;
import com.onestop.recommendation.web.dto.RecommendationDtos.RecommendationRow;
import com.onestop.recommendation.web.dto.RecommendationDtos.RecommendedProduct;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Explainable recommendations built from activity signals. No ML: every row is
 * a simple, auditable rule (popularity, view history) and always degrades to a
 * cold-start fallback so new and anonymous users still get something useful.
 */
@Service
public class RecommendationService {

    private final ProductSignalRepository productSignals;
    private final CustomerRecentViewRepository recentViews;
    private final CatalogClient catalog;

    public RecommendationService(ProductSignalRepository productSignals,
                                 CustomerRecentViewRepository recentViews,
                                 CatalogClient catalog) {
        this.productSignals = productSignals;
        this.recentViews = recentViews;
        this.catalog = catalog;
    }

    /** Most-viewed products across all users. Also the universal cold-start row. */
    public RecommendationRow trending(int limit) {
        List<Long> ids = productSignals.findAllByOrderByViewCountDescProductIdAsc(Limit.of(limit))
                .stream().map(ProductSignal::getProductId).toList();
        List<RecommendedProduct> items = enrich(ids, "Popular with shoppers right now", limit);
        return new RecommendationRow("trending", "Trending now", false, items);
    }

    /** A customer's recently viewed products, newest first. */
    public RecommendationRow recentlyViewed(Long customerId, int limit) {
        if (customerId == null) {
            return new RecommendationRow("recently-viewed", "Recently viewed", true, List.of());
        }
        List<Long> ids = recentViews.recentProductIds(customerId, limit);
        List<RecommendedProduct> items = enrich(ids, "You viewed this recently", limit);
        return new RecommendationRow("recently-viewed", "Recently viewed", false, items);
    }

    /**
     * Personalized row with a guaranteed result: a signed-in shopper's recent
     * views, topped up with trending; a new or anonymous shopper just gets
     * trending (the cold-start fallback).
     */
    public RecommendationRow forYou(Long customerId, int limit) {
        if (customerId == null) {
            RecommendationRow trending = trending(limit);
            return new RecommendationRow("for-you", "Recommended for you", true, trending.items());
        }

        List<Long> recent = recentViews.recentProductIds(customerId, limit);
        if (recent.isEmpty()) {
            RecommendationRow trending = trending(limit);
            return new RecommendationRow("for-you", "Recommended for you", true, trending.items());
        }

        // Personal history first, then fill the row with trending they haven't just seen.
        Set<Long> ordered = new LinkedHashSet<>(recent);
        if (ordered.size() < limit) {
            for (ProductSignal s : productSignals
                    .findAllByOrderByViewCountDescProductIdAsc(Limit.of(limit * 2))) {
                if (ordered.size() >= limit) {
                    break;
                }
                ordered.add(s.getProductId());
            }
        }
        List<RecommendedProduct> items = enrich(new ArrayList<>(ordered), "Based on what you viewed", limit);
        return new RecommendationRow("for-you", "Recommended for you", false, items);
    }

    /** Look up display details, drop products the catalog no longer serves. */
    private List<RecommendedProduct> enrich(List<Long> productIds, String reason, int limit) {
        List<RecommendedProduct> out = new ArrayList<>();
        for (Long id : productIds) {
            if (out.size() >= limit) {
                break;
            }
            Optional<CatalogClient.ProductInfo> info = catalog.findProduct(id);
            if (info.isEmpty()) {
                continue;
            }
            CatalogClient.ProductInfo p = info.get();
            out.add(new RecommendedProduct(p.id(), p.name(), p.brandName(), p.imageUrl(),
                    p.sellingPrice(), p.mrp(), reason));
        }
        return out;
    }
}
