package com.dev.LMS.dto;

import java.util.List;
import java.util.UUID;

public class StudentProgressSummaryDto {
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private int enrolledCourseCount;
    private int totalLessons;
    private int attendedLessons;
    private double attendanceRate;
    private int totalAssignments;
    private int submittedAssignments;
    private double assignmentCompletionRate;
    private int gradedAssignments;
    private Double averageAssignmentGrade;
    private int quizAttempts;
    private Double averageQuizGrade;
    private int unreadAlerts;
    private long behaviorReportCount;
    private long harmfulContentReportCount;
    private double overallProgressRate;
    private List<CourseProgressDto> courseProgress;

    public StudentProgressSummaryDto(UUID studentId,
                                     String studentName,
                                     String studentEmail,
                                     int enrolledCourseCount,
                                     int totalLessons,
                                     int attendedLessons,
                                     double attendanceRate,
                                     int totalAssignments,
                                     int submittedAssignments,
                                     double assignmentCompletionRate,
                                     int gradedAssignments,
                                     Double averageAssignmentGrade,
                                     int quizAttempts,
                                     Double averageQuizGrade,
                                     int unreadAlerts,
                                     long behaviorReportCount,
                                     long harmfulContentReportCount,
                                     double overallProgressRate,
                                     List<CourseProgressDto> courseProgress) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.enrolledCourseCount = enrolledCourseCount;
        this.totalLessons = totalLessons;
        this.attendedLessons = attendedLessons;
        this.attendanceRate = attendanceRate;
        this.totalAssignments = totalAssignments;
        this.submittedAssignments = submittedAssignments;
        this.assignmentCompletionRate = assignmentCompletionRate;
        this.gradedAssignments = gradedAssignments;
        this.averageAssignmentGrade = averageAssignmentGrade;
        this.quizAttempts = quizAttempts;
        this.averageQuizGrade = averageQuizGrade;
        this.unreadAlerts = unreadAlerts;
        this.behaviorReportCount = behaviorReportCount;
        this.harmfulContentReportCount = harmfulContentReportCount;
        this.overallProgressRate = overallProgressRate;
        this.courseProgress = courseProgress;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public int getEnrolledCourseCount() {
        return enrolledCourseCount;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public int getAttendedLessons() {
        return attendedLessons;
    }

    public double getAttendanceRate() {
        return attendanceRate;
    }

    public int getTotalAssignments() {
        return totalAssignments;
    }

    public int getSubmittedAssignments() {
        return submittedAssignments;
    }

    public double getAssignmentCompletionRate() {
        return assignmentCompletionRate;
    }

    public int getGradedAssignments() {
        return gradedAssignments;
    }

    public Double getAverageAssignmentGrade() {
        return averageAssignmentGrade;
    }

    public int getQuizAttempts() {
        return quizAttempts;
    }

    public Double getAverageQuizGrade() {
        return averageQuizGrade;
    }

    public int getUnreadAlerts() {
        return unreadAlerts;
    }

    public long getBehaviorReportCount() {
        return behaviorReportCount;
    }

    public long getHarmfulContentReportCount() {
        return harmfulContentReportCount;
    }

    public double getOverallProgressRate() {
        return overallProgressRate;
    }

    public List<CourseProgressDto> getCourseProgress() {
        return courseProgress;
    }
}
