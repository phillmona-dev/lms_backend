package com.dev.LMS.dto;

import com.dev.LMS.model.ParentStudentLink;

import java.util.UUID;

public class ParentStudentLinkDto {
    private Long id;
    private UUID parentId;
    private String parentName;
    private UUID studentId;
    private String studentName;
    private boolean active;

    public ParentStudentLinkDto(ParentStudentLink link) {
        this.id = link.getId();
        this.parentId = link.getParent().getPublicId();
        this.parentName = link.getParent().getName();
        this.studentId = link.getStudent().getPublicId();
        this.studentName = link.getStudent().getName();
        this.active = link.isActive();
    }

    public Long getId() {
        return id;
    }

    public UUID getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public boolean isActive() {
        return active;
    }
}
