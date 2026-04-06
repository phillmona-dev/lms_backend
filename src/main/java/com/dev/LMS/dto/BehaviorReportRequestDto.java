package com.dev.LMS.dto;

import com.dev.LMS.model.BehaviorSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BehaviorReportRequestDto {
    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "description is required")
    private String description;

    @NotNull(message = "severity is required")
    private BehaviorSeverity severity;

    private boolean harmfulContent;

    private String actionTaken;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BehaviorSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(BehaviorSeverity severity) {
        this.severity = severity;
    }

    public boolean isHarmfulContent() {
        return harmfulContent;
    }

    public void setHarmfulContent(boolean harmfulContent) {
        this.harmfulContent = harmfulContent;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }
}
