package com.hamtech.bookstorereviewservice.service.impl;

import com.hamtech.bookstorereviewservice.exception.AppException;
import com.hamtech.bookstorereviewservice.exception.ErrorCode;
import com.hamtech.bookstorereviewservice.model.dto.request.reviewrequest.CreateReviewRequest;
import com.hamtech.bookstorereviewservice.model.dto.response.reviewresponse.ReviewResponse;
import com.hamtech.bookstorereviewservice.model.entity.Review;
import com.hamtech.bookstorereviewservice.model.mapper.ReviewMapper;
import com.hamtech.bookstorereviewservice.repository.ReviewRepository;
import com.hamtech.bookstorereviewservice.service.ReviewService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewServiceImpl implements ReviewService {
    ReviewRepository reviewRepository;
    ReviewMapper reviewMapper;
    // UserRepository userRepository;
    // BookRepository bookRepository;

    @Transactional
    @Override
    public ReviewResponse addReviewBook(CreateReviewRequest request) {
        String authName = SecurityContextHolder.getContext().getAuthentication().getName();
        
        UUID userUUID;
        // Ưu tiên dùng userId từ body nếu có truyền lên (dùng để test)
        if (request.getUserId() != null) {
            userUUID = request.getUserId();
        } else if (authName == null || authName.equals("anonymousUser")) {
            userUUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); // Test User ID
        } else {
            userUUID = UUID.fromString(authName);
        }

        // Kiểm tra user đã đánh giá sách này chưa
        if (reviewRepository.existsByBookIDAndUserID(request.getBookId(), userUUID)) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = reviewMapper.toEntity(request);
        review.setUserID(userUUID);
        review.setBookID(request.getBookId());
    
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(savedReview);
    }


    @Override
    public Page<ReviewResponse> getReviewsByBookId(UUID bookId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId, pageable);
        return reviews.map(reviewMapper::toResponse);
    }
}
