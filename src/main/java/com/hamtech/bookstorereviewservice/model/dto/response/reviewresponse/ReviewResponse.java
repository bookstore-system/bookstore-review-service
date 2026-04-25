package com.hamtech.bookstorereviewservice.model.dto.response.reviewresponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    UUID reviewID;
    UUID userID;
    UUID bookID;
    String userName;
    String userAvatar;
    Integer rating;
    String comment;
    LocalDateTime createdAt;
    Boolean isVerifiedPurchase;
}