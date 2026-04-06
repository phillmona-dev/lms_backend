package com.dev.LMS.dto;

public class NationalExamSchoolResultDto {
    private Long schoolId;
    private String schoolName;
    private String region;
    private long candidates;
    private double averageScore;
    private double passRate;
    private double distinctionRate;
    private String readinessLevel;

    public NationalExamSchoolResultDto(Long schoolId,
                                       String schoolName,
                                       String region,
                                       long candidates,
                                       double averageScore,
                                       double passRate,
                                       double distinctionRate,
                                       String readinessLevel) {
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.region = region;
        this.candidates = candidates;
        this.averageScore = averageScore;
        this.passRate = passRate;
        this.distinctionRate = distinctionRate;
        this.readinessLevel = readinessLevel;
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

    public long getCandidates() {
        return candidates;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public double getPassRate() {
        return passRate;
    }

    public double getDistinctionRate() {
        return distinctionRate;
    }

    public String getReadinessLevel() {
        return readinessLevel;
    }
}
