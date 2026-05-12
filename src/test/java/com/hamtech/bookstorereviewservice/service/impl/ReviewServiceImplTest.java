package com.hamtech.bookstorereviewservice.service.impl;

import com.hamtech.bookstorereviewservice.exception.AppException;
import com.hamtech.bookstorereviewservice.exception.ErrorCode;
import com.hamtech.bookstorereviewservice.model.dto.request.reviewrequest.CreateReviewRequest;
import com.hamtech.bookstorereviewservice.model.dto.response.reviewresponse.ReviewResponse;
import com.hamtech.bookstorereviewservice.model.entity.Review;
import com.hamtech.bookstorereviewservice.model.mapper.ReviewMapper;
import com.hamtech.bookstorereviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    ReviewMapper reviewMapper;

    @InjectMocks
    ReviewServiceImpl reviewService;

    @BeforeEach
    void setupSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addReviewBook_success() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), "N/A"));

        CreateReviewRequest req = CreateReviewRequest.builder()
                .bookId(bookId)
                .rating(5)
                .comment("Sách rất hay và đáng đọc.")
                .build();

        when(reviewRepository.existsByBookIDAndUserID(bookId, userId)).thenReturn(false);

        Review mapped = Review.builder()
                .comment(req.getComment())
                .rating(req.getRating())
                .build();
        when(reviewMapper.toEntity(req)).thenReturn(mapped);

        Review saved = Review.builder()
                .reviewID(UUID.randomUUID())
                .bookID(bookId)
                .userID(userId)
                .comment(req.getComment())
                .rating(req.getRating())
                .verifiedPurchase(false)
                .build();
        when(reviewRepository.save(any(Review.class))).thenReturn(saved);

        ReviewResponse response = ReviewResponse.builder()
                .reviewID(saved.getReviewID())
                .bookID(bookId)
                .userID(userId)
                .rating(5)
                .comment(req.getComment())
                .build();
        when(reviewMapper.toResponse(saved)).thenReturn(response);

        ReviewResponse res = reviewService.addReviewBook(req);
        assertThat(res.getReviewID()).isEqualTo(saved.getReviewID());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void addReviewBook_throwsWhenAlreadyReviewed() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), "N/A"));

        CreateReviewRequest req = CreateReviewRequest.builder()
                .bookId(bookId)
                .rating(5)
                .comment("Sách rất hay và đáng đọc.")
                .build();

        when(reviewRepository.existsByBookIDAndUserID(bookId, userId)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.addReviewBook(req))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS);
    }
}
