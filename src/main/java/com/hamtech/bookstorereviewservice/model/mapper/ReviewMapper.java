package com.hamtech.bookstorereviewservice.model.mapper;

import com.hamtech.bookstorereviewservice.model.dto.request.reviewrequest.CreateReviewRequest;
import com.hamtech.bookstorereviewservice.model.dto.response.reviewresponse.ReviewResponse;
import com.hamtech.bookstorereviewservice.model.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "isVerifiedPurchase", source = "verifiedPurchase")
    ReviewResponse toResponse(Review review);

    @Mapping(target = "reviewID", ignore = true)
    @Mapping(target = "userID", ignore = true)
    @Mapping(target = "bookID", ignore = true)
    @Mapping(target = "helpfulCount", ignore = true)
    @Mapping(target = "verifiedPurchase", ignore = true)
    Review toEntity(CreateReviewRequest request);

}
