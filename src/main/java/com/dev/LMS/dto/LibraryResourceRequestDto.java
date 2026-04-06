package com.dev.LMS.dto;

import com.dev.LMS.model.LibraryAccessType;
import com.dev.LMS.model.LibraryResourceType;
import lombok.Data;

@Data
public class LibraryResourceRequestDto {
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
    private LibraryAccessType accessType;
    private Integer totalCopies;
}
