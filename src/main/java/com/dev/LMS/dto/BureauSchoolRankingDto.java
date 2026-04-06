package com.dev.LMS.dto;

public class BureauSchoolRankingDto {
    private int rank;
    private Long schoolId;
    private String schoolName;
    private String region;
    private double performanceScore;
    private double averageAttendanceRate;
    private double averageOverallProgressRate;
    private double examReadinessRate;
    private double complianceRate;
    private long atRiskStudents;

    public BureauSchoolRankingDto(int rank,
                                  Long schoolId,
                                  String schoolName,
                                  String region,
                                  double performanceScore,
                                  double averageAttendanceRate,
                                  double averageOverallProgressRate,
                                  double examReadinessRate,
                                  double complianceRate,
                                  long atRiskStudents) {
        this.rank = rank;
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.region = region;
        this.performanceScore = performanceScore;
        this.averageAttendanceRate = averageAttendanceRate;
        this.averageOverallProgressRate = averageOverallProgressRate;
        this.examReadinessRate = examReadinessRate;
        this.complianceRate = complianceRate;
        this.atRiskStudents = atRiskStudents;
    }

    public int getRank() {
        return rank;
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

    public double getPerformanceScore() {
        return performanceScore;
    }

    public double getAverageAttendanceRate() {
        return averageAttendanceRate;
    }

    public double getAverageOverallProgressRate() {
        return averageOverallProgressRate;
    }

    public double getExamReadinessRate() {
        return examReadinessRate;
    }

    public double getComplianceRate() {
        return complianceRate;
    }

    public long getAtRiskStudents() {
        return atRiskStudents;
    }
}
