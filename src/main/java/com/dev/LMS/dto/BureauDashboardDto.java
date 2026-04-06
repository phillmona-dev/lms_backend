package com.dev.LMS.dto;

public class BureauDashboardDto {
    private long totalSchools;
    private long totalStudents;
    private long totalTeachers;
    private long totalCourses;
    private double averageAttendanceRate;
    private double averageAssignmentCompletionRate;
    private double averageOverallProgressRate;
    private long totalBehaviorReports;
    private long totalHarmfulContentReports;
    private long totalAtRiskStudents;

    public BureauDashboardDto(long totalSchools,
                              long totalStudents,
                              long totalTeachers,
                              long totalCourses,
                              double averageAttendanceRate,
                              double averageAssignmentCompletionRate,
                              double averageOverallProgressRate,
                              long totalBehaviorReports,
                              long totalHarmfulContentReports,
                              long totalAtRiskStudents) {
        this.totalSchools = totalSchools;
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.totalCourses = totalCourses;
        this.averageAttendanceRate = averageAttendanceRate;
        this.averageAssignmentCompletionRate = averageAssignmentCompletionRate;
        this.averageOverallProgressRate = averageOverallProgressRate;
        this.totalBehaviorReports = totalBehaviorReports;
        this.totalHarmfulContentReports = totalHarmfulContentReports;
        this.totalAtRiskStudents = totalAtRiskStudents;
    }

    public long getTotalSchools() {
        return totalSchools;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public long getTotalTeachers() {
        return totalTeachers;
    }

    public long getTotalCourses() {
        return totalCourses;
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

    public long getTotalHarmfulContentReports() {
        return totalHarmfulContentReports;
    }

    public long getTotalAtRiskStudents() {
        return totalAtRiskStudents;
    }
}
