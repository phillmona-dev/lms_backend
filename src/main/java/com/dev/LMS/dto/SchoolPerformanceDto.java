package com.dev.LMS.dto;

public class SchoolPerformanceDto {
    private Long schoolId;
    private String schoolName;
    private String region;
    private long studentCount;
    private long teacherCount;
    private long courseCount;
    private double averageAttendanceRate;
    private double averageAssignmentCompletionRate;
    private double averageOverallProgressRate;
    private long behaviorReports;
    private long harmfulContentReports;
    private long atRiskStudents;

    public SchoolPerformanceDto(Long schoolId,
                                String schoolName,
                                String region,
                                long studentCount,
                                long teacherCount,
                                long courseCount,
                                double averageAttendanceRate,
                                double averageAssignmentCompletionRate,
                                double averageOverallProgressRate,
                                long behaviorReports,
                                long harmfulContentReports,
                                long atRiskStudents) {
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.region = region;
        this.studentCount = studentCount;
        this.teacherCount = teacherCount;
        this.courseCount = courseCount;
        this.averageAttendanceRate = averageAttendanceRate;
        this.averageAssignmentCompletionRate = averageAssignmentCompletionRate;
        this.averageOverallProgressRate = averageOverallProgressRate;
        this.behaviorReports = behaviorReports;
        this.harmfulContentReports = harmfulContentReports;
        this.atRiskStudents = atRiskStudents;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public String getRegion() {
        return region;
    }

    public long getStudentCount() {
        return studentCount;
    }

    public long getTeacherCount() {
        return teacherCount;
    }

    public long getCourseCount() {
        return courseCount;
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

    public long getBehaviorReports() {
        return behaviorReports;
    }

    public long getHarmfulContentReports() {
        return harmfulContentReports;
    }

    public long getAtRiskStudents() {
        return atRiskStudents;
    }
}
