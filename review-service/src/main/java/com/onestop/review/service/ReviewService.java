package com.onestop.review.service;

import com.onestop.review.domain.Review;
import com.onestop.review.repo.ReviewRepository;
import com.onestop.review.web.dto.ReviewDtos.CreateReviewRequest;
import com.onestop.review.web.dto.ReviewDtos.PagedResponse;
import com.onestop.review.web.dto.ReviewDtos.ReviewDto;
import com.onestop.review.web.dto.ReviewDtos.ReviewSummaryDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ReviewService {

    private final ReviewRepository reviews;

    public ReviewService(ReviewRepository reviews) {
        this.reviews = reviews;
    }

    /** Create the caller's review, or update it if they already reviewed this product. */
    @Transactional
    public ReviewDto submit(Long customerId, CreateReviewRequest req) {
        Review review = reviews.findByProductIdAndCustomerId(req.productId(), customerId)
                .orElseGet(Review::new);
        review.setProductId(req.productId());
        review.setCustomerId(customerId);
        review.setRating(req.rating().shortValue());
        review.setTitle(blankToNull(req.title()));
        review.setBody(blankToNull(req.body()));
        review.setUpdatedAt(OffsetDateTime.now());
        return toDto(reviews.save(review), customerId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewDto> list(Long productId, int page, int size, Long currentUserId) {
        var result = reviews.findByProductIdOrderByIdDesc(productId, PageRequest.of(page, size));
        var content = result.getContent().stream().map(r -> toDto(r, currentUserId)).toList();
        return new PagedResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isLast(), summary(productId));
    }

    @Transactional(readOnly = true)
    public ReviewSummaryDto summary(Long productId) {
        var s = reviews.summarize(productId);
        long count = s.getCount();
        double avg = (count == 0 || s.getAverage() == null) ? 0.0
                : Math.round(s.getAverage() * 10.0) / 10.0;
        return new ReviewSummaryDto(productId, count, avg);
    }

    private static ReviewDto toDto(Review r, Long currentUserId) {
        boolean mine = currentUserId != null && currentUserId.equals(r.getCustomerId());
        return new ReviewDto(r.getId(), r.getCustomerId(), r.getRating(), r.getTitle(),
                r.getBody(), r.getCreatedAt(), mine);
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
