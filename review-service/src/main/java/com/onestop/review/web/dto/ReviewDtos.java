package com.onestop.review.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public final class ReviewDtos {

    private ReviewDtos() {
    }

    public record CreateReviewRequest(
            @NotNull Long productId,
            @NotNull @Min(1) @Max(5) Integer rating,
            @Size(max = 150) String title,
            @Size(max = 4000) String body) {
    }

    public record ReviewDto(
            Long id,
            Long customerId,
            int rating,
            String title,
            String body,
            OffsetDateTime createdAt,
            boolean mine) {
    }

    public record ReviewSummaryDto(
            Long productId,
            long count,
            double average) {
    }

    public record PagedResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last,
            ReviewSummaryDto summary) {
    }
}
