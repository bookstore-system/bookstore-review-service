package com.hamtech.bookstorereviewservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamtech.bookstorereviewservice.model.dto.request.reviewrequest.CreateReviewRequest;
import com.hamtech.bookstorereviewservice.model.dto.response.reviewresponse.ReviewResponse;
import com.hamtech.bookstorereviewservice.model.dto.response.reviewresponse.UserReviewCountResponse;
import com.hamtech.bookstorereviewservice.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ReviewService reviewService;

    @Test
    void addReviewBook_returns200() throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        CreateReviewRequest req = CreateReviewRequest.builder()
                .bookId(bookId)
                .rating(5)
                .comment("Sách rất hay và đáng đọc.")
                .userId(userId)
                .build();

        ReviewResponse res = ReviewResponse.builder()
                .reviewID(reviewId)
                .userID(userId)
                .bookID(bookId)
                .rating(5)
                .comment(req.getComment())
                .createdAt(LocalDateTime.now())
                .isVerifiedPurchase(true)
                .build();

        when(reviewService.addReviewBook(any(CreateReviewRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/reviews/book/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.reviewID").value(reviewId.toString()))
                .andExpect(jsonPath("$.result.bookID").value(bookId.toString()))
                .andExpect(jsonPath("$.result.userID").value(userId.toString()));
    }

    @Test
    void getListReviewByBookId_returns200() throws Exception {
        UUID bookId = UUID.randomUUID();
        ReviewResponse r1 = ReviewResponse.builder()
                .reviewID(UUID.randomUUID())
                .bookID(bookId)
                .userID(UUID.randomUUID())
                .rating(4)
                .comment("Ổn áp.")
                .createdAt(LocalDateTime.now())
                .build();

        Page<ReviewResponse> page = new PageImpl<>(List.of(r1), PageRequest.of(0, 10), 1);
        when(reviewService.getReviewsByBookId(eq(bookId), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/v1/reviews/book/{bookId}", bookId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.content[0].bookID").value(bookId.toString()))
                .andExpect(jsonPath("$.result.content[0].comment").value("Ổn áp."));
    }

    @Test
    void countReviewsByUserId_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(reviewService.countReviewsByUserId(eq(userId))).thenReturn(7L);

        mockMvc.perform(get("/api/v1/reviews/users/{userId}/count", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.totalReviews").value(7));
    }
}

