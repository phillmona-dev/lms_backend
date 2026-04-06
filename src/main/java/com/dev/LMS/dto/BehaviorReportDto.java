package com.dev.LMS.dto;

import com.dev.LMS.model.BehaviorReport;
import com.dev.LMS.model.BehaviorSeverity;
import com.dev.LMS.model.BehaviorStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class BehaviorReportDto {
    private Long id;
    private UUID studentId;
    private String studentName;
    private UUID teacherId;
    private String teacherName;
    private Long courseId;
    private String courseName;
    private String title;
    private String description;
    private BehaviorSeverity severity;
    private BehaviorStatus status;
    private boolean harmfulContent;
    private String actionTaken;
    private LocalDateTime createdAt;

    public BehaviorReportDto(BehaviorReport report) {
        this.id = report.getId();
        this.studentId = report.getStudent().getPublicId();
        this.studentName = report.getStudent().getName();
        this.teacherId = report.getTeacher().getPublicId();
        this.teacherName = report.getTeacher().getName();
        this.courseId = report.getCourse() == null ? null : report.getCourse().getCourseId();
        this.courseName = report.getCourse() == null ? null : report.getCourse().getName();
        this.title = report.getTitle();
        this.description = report.getDescription();
        this.severity = report.getSeverity();
        this.status = report.getStatus();
        this.harmfulContent = report.isHarmfulContent();
        this.actionTaken = report.getActionTaken();
        this.createdAt = report.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BehaviorSeverity getSeverity() {
        return severity;
    }

    public BehaviorStatus getStatus() {
        return status;
    }

    public boolean isHarmfulContent() {
        return harmfulContent;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
