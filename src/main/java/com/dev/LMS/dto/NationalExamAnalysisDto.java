package com.dev.LMS.dto;

import java.util.List;

public class NationalExamAnalysisDto {
    private double nationalAverageScore;
    private double nationalPassRate;
    private long totalCandidates;
    private long highPerformingSchools;
    private long schoolsNeedingSupport;
    private String keyInsight;
    private List<NationalExamSchoolResultDto> topSchools;
    private List<NationalExamSchoolResultDto> schoolResults;

    public NationalExamAnalysisDto(double nationalAverageScore,
                                   double nationalPassRate,
                                   long totalCandidates,
                                   long highPerformingSchools,
                                   long schoolsNeedingSupport,
                                   String keyInsight,
                                   List<NationalExamSchoolResultDto> topSchools,
                                   List<NationalExamSchoolResultDto> schoolResults) {
        this.nationalAverageScore = nationalAverageScore;
        this.nationalPassRate = nationalPassRate;
        this.totalCandidates = totalCandidates;
        this.highPerformingSchools = highPerformingSchools;
        this.schoolsNeedingSupport = schoolsNeedingSupport;
        this.keyInsight = keyInsight;
        this.topSchools = topSchools;
        this.schoolResults = schoolResults;
    }

    public double getNationalAverageScore() {
        return nationalAverageScore;
    }

    public double getNationalPassRate() {
        return nationalPassRate;
    }

    public long getTotalCandidates() {
        return totalCandidates;
    }

    public long getHighPerformingSchools() {
        return highPerformingSchools;
    }

    public long getSchoolsNeedingSupport() {
        return schoolsNeedingSupport;
    }

    public String getKeyInsight() {
        return keyInsight;
    }

    public List<NationalExamSchoolResultDto> getTopSchools() {
        return topSchools;
    }

    public List<NationalExamSchoolResultDto> getSchoolResults() {
        return schoolResults;
    }
}
