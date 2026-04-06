package com.dev.LMS.dto;

public class SchoolAdminDashboardDto {
    private long totalUsers;
    private long totalStudents;
    private long totalTeachers;
    private long totalParents;
    private long totalCourses;
    private long totalEnrollments;
    private double averageAttendanceRate;
    private double averageAssignmentCompletionRate;
    private double averageOverallProgressRate;
    private long totalBehaviorReports;
    private long harmfulContentReports;
    private long studentsAtRisk;

    public SchoolAdminDashboardDto(long totalUsers,
                                   long totalStudents,
                                   long totalTeachers,
                                   long totalParents,
                                   long totalCourses,
                                   long totalEnrollments,
                                   double averageAttendanceRate,
                                   double averageAssignmentCompletionRate,
                                   double averageOverallProgressRate,
                                   long totalBehaviorReports,
                                   long harmfulContentReports,
                                   long studentsAtRisk) {
        this.totalUsers = totalUsers;
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.totalParents = totalParents;
        this.totalCourses = totalCourses;
        this.totalEnrollments = totalEnrollments;
        this.averageAttendanceRate = averageAttendanceRate;
        this.averageAssignmentCompletionRate = averageAssignmentCompletionRate;
        this.averageOverallProgressRate = averageOverallProgressRate;
        this.totalBehaviorReports = totalBehaviorReports;
        this.harmfulContentReports = harmfulContentReports;
        this.studentsAtRisk = studentsAtRisk;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public long getTotalTeachers() {
        return totalTeachers;
    }

    public long getTotalParents() {
        return totalParents;
    }

    public long getTotalCourses() {
        return totalCourses;
    }

    public long getTotalEnrollments() {
        return totalEnrollments;
    }

    public double getAverageAttendanceRate() {
        return averageAttendanceRate;
    }

    public double getAverageAssignmentCompletionRate() {
        return averageAssignmentCompletionRate;
    }

    public double getAverageOverallProgressRate() {
        return averageOverallProgressRate;
    }

    public long getTotalBehaviorReports() {
        return totalBehaviorReports;
    }

    public long getHarmfulContentReports() {
        return harmfulContentReports;
    }

    public long getStudentsAtRisk() {
        return studentsAtRisk;
    }
}
