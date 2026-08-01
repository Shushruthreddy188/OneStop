package com.onestop.review.web;

import com.onestop.review.service.ReviewService;
import com.onestop.review.web.dto.ReviewDtos.CreateReviewRequest;
import com.onestop.review.web.dto.ReviewDtos.PagedResponse;
import com.onestop.review.web.dto.ReviewDtos.ReviewDto;
import com.onestop.review.web.dto.ReviewDtos.ReviewSummaryDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final int MAX_PAGE_SIZE = 50;

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** GET /api/reviews?productId=&page=&size= — public list + rating summary. */
    @GetMapping
    public PagedResponse<ReviewDto> list(@RequestParam Long productId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @AuthenticationPrincipal Long currentUserId) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return reviewService.list(productId, safePage, safeSize, currentUserId);
    }

    /** GET /api/reviews/summary?productId= — public average + count. */
    @GetMapping("/summary")
    public ReviewSummaryDto summary(@RequestParam Long productId) {
        return reviewService.summary(productId);
    }

    /** POST /api/reviews — create or update the caller's review (auth). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto submit(@AuthenticationPrincipal Long customerId,
                            @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.submit(customerId, request);
    }
}
