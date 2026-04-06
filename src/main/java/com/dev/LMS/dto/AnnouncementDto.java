package com.dev.LMS.dto;

import com.dev.LMS.model.Announcement;

import java.time.LocalDateTime;
import java.util.UUID;

public class AnnouncementDto {
    private Long id;
    private String title;
    private String message;
    private String audience;
    private UUID createdById;
    private String createdByName;
    private LocalDateTime createdAt;

    public AnnouncementDto(Announcement announcement) {
        this.id = announcement.getId();
        this.title = announcement.getTitle();
        this.message = announcement.getMessage();
        this.audience = announcement.getAudience();
        this.createdById = announcement.getCreatedBy().getPublicId();
        this.createdByName = announcement.getCreatedBy().getName();
        this.createdAt = announcement.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getAudience() {
        return audience;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
