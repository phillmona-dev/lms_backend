package com.dev.LMS.dto;

import jakarta.validation.constraints.NotBlank;

public class PrivilegeRequestDto {
    @NotBlank(message = "Privilege name is required")
    private String name;

    @NotBlank(message = "Privilege description is required")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
