package com.dev.LMS.dto;

import com.dev.LMS.model.LibraryBorrow;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LibraryBorrowDto {
    private Long id;
    private Long resourceId;
    private String resourceTitle;
    private String resourceAuthor;
    private String status;
    private LocalDateTime borrowedAt;
    private LocalDateTime dueDate;
    private LocalDateTime returnedAt;

    public static LibraryBorrowDto from(LibraryBorrow borrow) {
        return LibraryBorrowDto.builder()
                .id(borrow.getId())
                .resourceId(borrow.getResource().getId())
                .resourceTitle(borrow.getResource().getTitle())
                .resourceAuthor(borrow.getResource().getAuthor())
                .status(borrow.getStatus().name())
                .borrowedAt(borrow.getBorrowedAt())
                .dueDate(borrow.getDueDate())
                .returnedAt(borrow.getReturnedAt())
                .build();
    }
}
