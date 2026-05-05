package com.hamtech.bookstorereviewservice.model.dto.response.reviewresponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserReviewCountResponse {
    UUID userId;
    long totalReviews;
}
