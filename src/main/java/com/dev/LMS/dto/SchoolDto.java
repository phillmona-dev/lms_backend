package com.dev.LMS.dto;

import com.dev.LMS.model.School;

public class SchoolDto {
    private Long id;
    private String name;
    private String region;
    private String code;

    public SchoolDto(School school) {
        this.id = school.getId();
        this.name = school.getName();
        this.region = school.getRegion();
        this.code = school.getCode();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public String getCode() {
        return code;
    }
}
