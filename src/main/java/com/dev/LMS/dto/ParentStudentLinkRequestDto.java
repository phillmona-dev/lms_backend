package com.dev.LMS.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class ParentStudentLinkRequestDto {
    @NotNull(message = "parentId is required")
    private UUID parentId;

    @NotNull(message = "studentId is required")
    private UUID studentId;

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }
}
