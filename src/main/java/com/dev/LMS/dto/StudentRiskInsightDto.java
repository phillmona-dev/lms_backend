package com.dev.LMS.dto;

import java.util.UUID;

public class StudentRiskInsightDto {
    private UUID studentId;
    private String studentName;
    private String riskLevel;
    private String reason;
    private double attendanceRate;
    private double assignmentCompletionRate;
    private long behaviorReportCount;
    private long harmfulContentReportCount;

    public StudentRiskInsightDto(UUID studentId,
                                 String studentName,
                                 String riskLevel,
                                 String reason,
                                 double attendanceRate,
                                 double assignmentCompletionRate,
                                 long behaviorReportCount,
                                 long harmfulContentReportCount) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.riskLevel = riskLevel;
        this.reason = reason;
        this.attendanceRate = attendanceRate;
        this.assignmentCompletionRate = assignmentCompletionRate;
        this.behaviorReportCount = behaviorReportCount;
        this.harmfulContentReportCount = harmfulContentReportCount;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getReason() {
        return reason;
    }

    public double getAttendanceRate() {
        return attendanceRate;
    }

    public double getAssignmentCompletionRate() {
        return assignmentCompletionRate;
    }

    public long getBehaviorReportCount() {
        return behaviorReportCount;
    }

    public long getHarmfulContentReportCount() {
        return harmfulContentReportCount;
    }
}
