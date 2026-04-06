package com.dev.LMS.dto;

import jakarta.validation.constraints.NotBlank;

public class SchoolRequestDto {
    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "region is required")
    private String region;

    @NotBlank(message = "code is required")
    private String code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
