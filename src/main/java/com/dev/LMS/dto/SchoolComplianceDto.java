package com.dev.LMS.dto;

public class SchoolComplianceDto {
    private Long schoolId;
    private String schoolName;
    private String region;
    private double curriculumComplianceRate;
    private double attendanceComplianceRate;
    private double safetyComplianceRate;
    private double dataReportingComplianceRate;
    private double overallComplianceRate;
    private String status;
    private String notes;

    public SchoolComplianceDto(Long schoolId,
                               String schoolName,
                               String region,
                               double curriculumComplianceRate,
                               double attendanceComplianceRate,
                               double safetyComplianceRate,
                               double dataReportingComplianceRate,
                               double overallComplianceRate,
                               String status,
                               String notes) {
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.region = region;
        this.curriculumComplianceRate = curriculumComplianceRate;
        this.attendanceComplianceRate = attendanceComplianceRate;
        this.safetyComplianceRate = safetyComplianceRate;
        this.dataReportingComplianceRate = dataReportingComplianceRate;
        this.overallComplianceRate = overallComplianceRate;
        this.status = status;
        this.notes = notes;
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

    public double getCurriculumComplianceRate() {
        return curriculumComplianceRate;
    }

    public double getAttendanceComplianceRate() {
        return attendanceComplianceRate;
    }

    public double getSafetyComplianceRate() {
        return safetyComplianceRate;
    }

    public double getDataReportingComplianceRate() {
        return dataReportingComplianceRate;
    }

    public double getOverallComplianceRate() {
        return overallComplianceRate;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }
}
