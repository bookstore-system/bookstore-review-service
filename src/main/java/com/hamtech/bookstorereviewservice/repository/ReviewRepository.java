package com.hamtech.bookstorereviewservice.repository;

import com.hamtech.bookstorereviewservice.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Đánh giá của sách (sắp xếp theo thời gian mới nhất)
    @Query("SELECT r FROM Review r WHERE r.bookID = :bookId ORDER BY r.createdAt DESC")
    Page<Review> findByBookIdOrderByCreatedAtDesc(@Param("bookId") UUID bookId, Pageable pageable);

    // Kiểm tra user đã đánh giá chưa
    Optional<Review> findByBookIDAndUserID(UUID bookID, UUID userID);
    boolean existsByBookIDAndUserID(UUID bookID, UUID userID);

    // Tính trung bình sao
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.bookID = :bookId")
    Double calculateAverageRating(@Param("bookId") UUID bookId);

    // Đếm số đánh giá
    Long countByBookID(UUID bookID);

    // Đánh giá theo số sao
    Long countByBookIDAndRating(UUID bookID, Integer rating);
}

