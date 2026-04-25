package com.hamtech.bookstorereviewservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// @ToString(exclude = {"user", "book"})
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {

    @Id
    @UuidGenerator
    @Column(name = "review_id")
    UUID reviewID;

    @Column(columnDefinition = "TEXT")
    String comment;

    @Column(nullable = false)
    Integer rating;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "is_verified_purchase", nullable = false)
    boolean verifiedPurchase = false;

    @Column(name = "helpful_count")
    Integer helpfulCount = 0;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "user_id", nullable = false)
    // @JsonBackReference
    // User user;

    @Column(name="user_id")
    private UUID userID;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "book_id", nullable = false)
    // @JsonBackReference
    // Book book;

    @Column(name="book_id")
    private UUID bookID;

    @Column(name = "user_name")
    String userName;

    @Column(name = "user_avatar")
    String userAvatar;
}
