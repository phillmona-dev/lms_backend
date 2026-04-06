package com.dev.LMS.dto;

import java.util.List;

public class SchoolCurriculumDto {
    private Long schoolId;
    private String schoolName;
    private String region;
    private long totalCourses;
    private long totalLessons;
    private long totalAssignments;
    private double averageLessonsPerCourse;
    private double curriculumCoverageRate;
    private double coreSubjectCoverageRate;
    private List<String> missingCoreSubjects;

    public SchoolCurriculumDto(Long schoolId,
                               String schoolName,
                               String region,
                               long totalCourses,
                               long totalLessons,
                               long totalAssignments,
                               double averageLessonsPerCourse,
                               double curriculumCoverageRate,
                               double coreSubjectCoverageRate,
                               List<String> missingCoreSubjects) {
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.region = region;
        this.totalCourses = totalCourses;
        this.totalLessons = totalLessons;
        this.totalAssignments = totalAssignments;
        this.averageLessonsPerCourse = averageLessonsPerCourse;
        this.curriculumCoverageRate = curriculumCoverageRate;
        this.coreSubjectCoverageRate = coreSubjectCoverageRate;
        this.missingCoreSubjects = missingCoreSubjects;
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

    public long getTotalCourses() {
        return totalCourses;
    }

    public long getTotalLessons() {
        return totalLessons;
    }

    public long getTotalAssignments() {
        return totalAssignments;
    }

    public double getAverageLessonsPerCourse() {
        return averageLessonsPerCourse;
    }

    public double getCurriculumCoverageRate() {
        return curriculumCoverageRate;
    }

    public double getCoreSubjectCoverageRate() {
        return coreSubjectCoverageRate;
    }

    public List<String> getMissingCoreSubjects() {
        return missingCoreSubjects;
    }
}
