package com.dev.LMS.dto;

public class AiInsightDto {
    private String category;
    private String severity;
    private String summary;
    private String recommendation;

    public AiInsightDto(String category, String severity, String summary, String recommendation) {
        this.category = category;
        this.severity = severity;
        this.summary = summary;
        this.recommendation = recommendation;
    }

    public String getCategory() {
        return category;
    }

    public String getSeverity() {
        return severity;
    }

    public String getSummary() {
        return summary;
    }

    public String getRecommendation() {
        return recommendation;
    }
}
