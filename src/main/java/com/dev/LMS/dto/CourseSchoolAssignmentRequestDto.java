package com.dev.LMS.dto;

import jakarta.validation.constraints.NotNull;

public class CourseSchoolAssignmentRequestDto {
    @NotNull(message = "courseId is required")
    private Integer courseId;

    @NotNull(message = "schoolId is required")
    private Long schoolId;

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }
}
