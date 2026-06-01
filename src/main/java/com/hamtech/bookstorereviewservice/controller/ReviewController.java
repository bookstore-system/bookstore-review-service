package com.hamtech.bookstorereviewservice.controller;

import com.hamtech.bookstorereviewservice.model.dto.request.reviewrequest.CreateReviewRequest;
import com.hamtech.bookstorereviewservice.model.dto.response.ApiResponse;
import com.hamtech.bookstorereviewservice.model.dto.response.reviewresponse.ReviewResponse;
import com.hamtech.bookstorereviewservice.model.dto.response.reviewresponse.ReviewSummaryResponse;
import com.hamtech.bookstorereviewservice.model.dto.response.reviewresponse.UserReviewCountResponse;
import com.hamtech.bookstorereviewservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller xử lý các chức năng liên quan đến đánh giá sách
 * Cho phép người dùng thêm đánh giá và xem danh sách đánh giá của sách
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Reviews", description = "APIs quản lý đánh giá sách")
public class ReviewController {
    ReviewService reviewService;

    /**
     * Thêm đánh giá mới cho một cuốn sách
     * Người dùng cần đăng nhập và đã mua sách mới có thể đánh giá
     *
     * @param request Thông tin đánh giá bao gồm bookId, rating (1-5 sao), content
     * @return Thông tin đánh giá vừa được tạo
     */
    @PostMapping("/book/add")
    @Operation(summary = "Thêm đánh giá cho sách", description = "Yêu cầu người dùng đã mua sách và chưa đánh giá trước đó.")
    public ApiResponse<ReviewResponse> addReviewBook(@Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse review = reviewService.addReviewBook(request);
        return ApiResponse.<ReviewResponse>builder()
                .code(1000)
                .message("Thêm đánh giá thành công")
                .result(review)
                .build();
    }

    /**
     * Lấy danh sách đánh giá của một cuốn sách
     * Hỗ trợ phân trang để hiển thị danh sách đánh giá
     *
     * @param bookId ID của sách cần xem đánh giá
     * @param page Số trang (mặc định: 0)
     * @param size Kích thước trang (mặc định: 10)
     * @return Danh sách đánh giá được phân trang bao gồm thông tin người đánh giá, rating, nội dung và thời gian
     */
    @GetMapping("/order/{orderId}/reviewed-books")
    @Operation(summary = "Sách đã đánh giá trong đơn", description = "Trả về danh sách bookId đã review trong orderId (ẩn form trên UI đơn hàng).")
    public ApiResponse<List<UUID>> getReviewedBookIdsByOrder(@PathVariable UUID orderId) {
        List<UUID> bookIds = reviewService.getReviewedBookIdsByOrderId(orderId);
        return ApiResponse.<List<UUID>>builder()
                .code(1000)
                .message("Lấy danh sách sách đã đánh giá thành công")
                .result(bookIds)
                .build();
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Đánh giá theo đơn hàng", description = "Trả về toàn bộ review đã gửi trong đơn (hiển thị trên trang chi tiết đơn).")
    public ApiResponse<List<ReviewResponse>> getReviewsByOrder(@PathVariable UUID orderId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByOrderId(orderId);
        return ApiResponse.<List<ReviewResponse>>builder()
                .code(1000)
                .message("Lấy đánh giá theo đơn hàng thành công")
                .result(reviews)
                .build();
    }

    @GetMapping("/book/{bookId}")
    @Operation(summary = "Lấy danh sách đánh giá theo bookId", description = "Trả về danh sách đánh giá theo phân trang, sắp xếp mới nhất trước.")
    public ApiResponse<Page<ReviewResponse>> getListReviewByBookId(
            @PathVariable UUID bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReviewResponse> reviews = reviewService.getReviewsByBookId(bookId, page, size);
        return ApiResponse.<Page<ReviewResponse>>builder()
                .code(1000)
                .message("Lấy danh sách đánh giá thành công")
                .result(reviews)
                .build();
    }

    @GetMapping("/book/{bookId}/summary")
    @Operation(summary = "Tong hop danh gia theo bookId", description = "Tra ve averageRating, totalReviews va phan bo sao.")
    public ApiResponse<ReviewSummaryResponse> getReviewSummaryByBookId(@PathVariable UUID bookId) {
        ReviewSummaryResponse summary = reviewService.getReviewSummaryByBookId(bookId);
        return ApiResponse.<ReviewSummaryResponse>builder()
                .code(1000)
                .message("Lay tong hop danh gia thanh cong")
                .result(summary)
                .build();
    }

    /**
     * Đếm tổng số đánh giá của một người dùng
     * 
     * @param userId ID của người dùng
     * @return Số lượng đánh giá (trả về 0 nếu chưa có)
     */
    @GetMapping("/users/{userId}/count")
    @Operation(summary = "Đếm số review của user", description = "Trả về tổng số review mà user đã tạo.")
    public UserReviewCountResponse countReviewsByUserId(@PathVariable UUID userId) {
        long totalReviews = reviewService.countReviewsByUserId(userId);
        return UserReviewCountResponse.builder()
                .userId(userId)
                .totalReviews(totalReviews)
                .build();
    }
}
