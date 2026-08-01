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

    public record ReindexResult(long indexed) {
    }
}
