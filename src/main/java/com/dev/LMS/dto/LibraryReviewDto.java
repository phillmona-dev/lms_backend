package com.dev.LMS.dto;

import com.dev.LMS.model.LibraryReview;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LibraryReviewDto {
    private Long id;
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LibraryReviewDto from(LibraryReview review) {
        return LibraryReviewDto.builder()
                .id(review.getId())
                .userName(review.getUser() != null ? review.getUser().getName() : "User")
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
