package com.onestop.review.service;

import com.onestop.review.domain.Review;
import com.onestop.review.repo.ReviewRepository;
import com.onestop.review.web.dto.ReviewDtos.CreateReviewRequest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReviewServiceTest {
    @Test
    void submittingAgainUpdatesTheCustomersExistingReview() {
        ReviewRepository repo = mock(ReviewRepository.class);
        Review existing = new Review();
        existing.setId(9L);
        existing.setProductId(3L);
        existing.setCustomerId(4L);
        when(repo.findByProductIdAndCustomerId(3L, 4L)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        var result = new ReviewService(repo).submit(4L,
                new CreateReviewRequest(3L, 5, "  Great  ", "  Worth it  "));

        assertThat(result.id()).isEqualTo(9L);
        assertThat(result.mine()).isTrue();
        assertThat(existing.getRating()).isEqualTo((short) 5);
        assertThat(existing.getTitle()).isEqualTo("Great");
        verify(repo).save(existing);
    }
}
