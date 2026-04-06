package com.dev.LMS.dto;

import com.dev.LMS.model.LibraryResource;
import com.dev.LMS.model.LibraryResourceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LibraryResourceDto {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private String description;
    private LibraryResourceType resourceType;
    private String category;
    private String subject;
    private String gradeLevel;
    private String tags;
    private String language;
    private Integer publicationYear;
    private Integer pages;
    private String externalUrl;
    private String accessType;
    private Integer totalCopies;
    private Integer availableCopies;
    private boolean available;
    private String fileName;
    private String fileType;
    private String coverFileName;
    private String coverFileType;
    private String createdByName;
    private Double averageRating;
    private Long ratingsCount;
    private Long favoritesCount;
    private boolean favoritedByMe;
    private boolean borrowedByMe;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LibraryResourceDto from(
            LibraryResource resource,
            Double averageRating,
            Long ratingsCount,
            Long favoritesCount,
            boolean favoritedByMe,
            boolean borrowedByMe
    ) {
        return LibraryResourceDto.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .author(resource.getAuthor())
                .publisher(resource.getPublisher())
                .isbn(resource.getIsbn())
                .description(resource.getDescription())
                .resourceType(resource.getResourceType())
                .category(resource.getCategory())
                .subject(resource.getSubject())
                .gradeLevel(resource.getGradeLevel())
                .tags(resource.getTags())
                .language(resource.getLanguage())
                .publicationYear(resource.getPublicationYear())
                .pages(resource.getPages())
                .externalUrl(resource.getExternalUrl())
                .accessType(resource.getAccessType() != null ? resource.getAccessType().name() : null)
                .totalCopies(resource.getTotalCopies())
                .availableCopies(resource.getAvailableCopies())
                .available(resource.getAvailableCopies() != null && resource.getAvailableCopies() > 0)
                .fileName(resource.getFileName())
                .fileType(resource.getFileType())
                .coverFileName(resource.getCoverFileName())
                .coverFileType(resource.getCoverFileType())
                .createdByName(resource.getCreatedBy() != null ? resource.getCreatedBy().getName() : null)
                .averageRating(averageRating)
                .ratingsCount(ratingsCount)
                .favoritesCount(favoritesCount)
                .favoritedByMe(favoritedByMe)
                .borrowedByMe(borrowedByMe)
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }
}
