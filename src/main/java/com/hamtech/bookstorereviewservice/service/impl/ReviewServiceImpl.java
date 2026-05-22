package com.hamtech.bookstorereviewservice.service.impl;

import com.hamtech.bookstorereviewservice.client.UserBasicInfoResponse;
import com.hamtech.bookstorereviewservice.client.UserServiceClient;
import com.hamtech.bookstorereviewservice.exception.AppException;
import com.hamtech.bookstorereviewservice.exception.ErrorCode;
import com.hamtech.bookstorereviewservice.model.dto.request.reviewrequest.CreateReviewRequest;
import com.hamtech.bookstorereviewservice.model.dto.response.ApiResponse;
import com.hamtech.bookstorereviewservice.model.dto.response.reviewresponse.ReviewResponse;
import com.hamtech.bookstorereviewservice.model.entity.Review;
import com.hamtech.bookstorereviewservice.model.mapper.ReviewMapper;
import com.hamtech.bookstorereviewservice.repository.ReviewRepository;
import com.hamtech.bookstorereviewservice.service.ReviewService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewServiceImpl implements ReviewService {
    ReviewRepository reviewRepository;
    ReviewMapper reviewMapper;
    UserServiceClient userServiceClient;

    @Transactional
    @Override
    public ReviewResponse addReviewBook(CreateReviewRequest request) {
        UUID userUUID = resolveCurrentUserId(request);

        if (reviewRepository.existsByOrderIDAndBookID(request.getOrderId(), request.getBookId())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = reviewMapper.toEntity(request);
        review.setUserID(userUUID);
        review.setBookID(request.getBookId());
        review.setOrderID(request.getOrderId());
        review.setVerifiedPurchase(true);
        enrichUserProfile(review, userUUID, request);

        Review savedReview = reviewRepository.save(review);
        return toEnrichedResponse(savedReview);
    }

    @Override
    public Page<ReviewResponse> getReviewsByBookId(UUID bookId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId, pageable);
        return reviews.map(this::toEnrichedResponse);
    }

    @Override
    public List<UUID> getReviewedBookIdsByOrderId(UUID orderId) {
        return reviewRepository.findBookIdsByOrderID(orderId);
    }

    @Override
    public List<ReviewResponse> getReviewsByOrderId(UUID orderId) {
        return reviewRepository.findByOrderIDOrderByCreatedAtDesc(orderId).stream()
                .map(this::toEnrichedResponse)
                .toList();
    }

    /**
     * Bổ sung userName/avatar từ user-service khi DB null (review cũ hoặc Feign lỗi lúc tạo).
     * Ghi lại DB một lần để lần sau không gọi lại.
     */
    private ReviewResponse toEnrichedResponse(Review review) {
        ReviewResponse response = reviewMapper.toResponse(review);
        if (review.getUserID() == null) {
            return response;
        }

        boolean missingName = !StringUtils.hasText(review.getUserName());
        boolean missingAvatar = !StringUtils.hasText(review.getUserAvatar());
        if (!missingName && !missingAvatar) {
            return response;
        }

        UserBasicInfoResponse info = fetchUserBasicInfo(review.getUserID());
        if (info == null) {
            return response;
        }

        boolean changed = false;
        if (missingName && StringUtils.hasText(info.getDisplayName())) {
            review.setUserName(info.getDisplayName().trim());
            response.setUserName(review.getUserName());
            changed = true;
        }
        if (missingAvatar && StringUtils.hasText(info.getAvatarUrl())) {
            review.setUserAvatar(info.getAvatarUrl().trim());
            response.setUserAvatar(review.getUserAvatar());
            changed = true;
        }
        if (changed) {
            reviewRepository.save(review);
        }
        return response;
    }

    private UserBasicInfoResponse fetchUserBasicInfo(UUID userId) {
        try {
            ApiResponse<UserBasicInfoResponse> response = userServiceClient.getUserBasicInfo(userId);
            if (response != null && response.getResult() != null) {
                return response.getResult();
            }
        } catch (Exception ex) {
            log.warn("Could not load user basic-info for userId={}: {}", userId, ex.getMessage());
        }
        return null;
    }

    private void enrichUserProfile(Review review, UUID userId, CreateReviewRequest request) {
        if (StringUtils.hasText(request.getUserName())) {
            review.setUserName(request.getUserName().trim());
        }
        if (StringUtils.hasText(request.getUserAvatar())) {
            review.setUserAvatar(request.getUserAvatar().trim());
        }
        if (StringUtils.hasText(review.getUserName()) && StringUtils.hasText(review.getUserAvatar())) {
            return;
        }

        UserBasicInfoResponse info = fetchUserBasicInfo(userId);
        if (info == null) {
            return;
        }
        if (!StringUtils.hasText(review.getUserName()) && StringUtils.hasText(info.getDisplayName())) {
            review.setUserName(info.getDisplayName().trim());
        }
        if (!StringUtils.hasText(review.getUserAvatar()) && StringUtils.hasText(info.getAvatarUrl())) {
            review.setUserAvatar(info.getAvatarUrl().trim());
        }
    }

    private UUID resolveCurrentUserId(CreateReviewRequest request) {
        if (request.getUserId() != null) {
            return request.getUserId();
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String userId = jwt.getClaimAsString("userId");
            if (StringUtils.hasText(userId)) {
                return UUID.fromString(userId);
            }
        }
        String principal = authentication.getName();
        if (!StringUtils.hasText(principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        try {
            return UUID.fromString(principal);
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    @Override
    public long countReviewsByUserId(UUID userId) {
        Long count = reviewRepository.countByUserID(userId);
        return count != null ? count : 0L;
    }
}
