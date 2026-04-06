package com.dev.LMS.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class UserSchoolAssignmentRequestDto {
    @NotNull(message = "userId is required")
    private UUID userId;

    @NotNull(message = "schoolId is required")
    private Long schoolId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }
}
