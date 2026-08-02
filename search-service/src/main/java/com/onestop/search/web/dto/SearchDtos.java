package com.onestop.search.web.dto;

import java.math.BigDecimal;
import java.util.List;

public final class SearchDtos {

    private SearchDtos() {
    }

    public record SuggestionDto(Long productId, String name) {
    }

    public record SearchResultDto(
            Long productId,
            String name,
            String brandName,
            String categoryName,
            String packageSize,
            BigDecimal sellingPrice,
            BigDecimal mrp) {
    }

    public record PagedResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last) {
    }

    /** A single facet value and how many results carry it. */
    public record Facet(String value, long count) {
    }

    /**
     * Search results plus brand/category facet counts. Field names mirror
     * {@link PagedResponse} so existing clients keep working; {@code brands} and
     * {@code categories} are additive (empty from the Postgres fallback path).
     */
    public record FacetedSearchResponse(
            List<SearchResultDto> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last,
            List<Facet> brands,
            List<Facet> categories) {
    }

    public record ReindexResult(long indexed) {
    }
}
