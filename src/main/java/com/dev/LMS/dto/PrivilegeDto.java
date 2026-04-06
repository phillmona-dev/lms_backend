package com.dev.LMS.dto;

import com.dev.LMS.model.Privilege;

import java.util.UUID;

public class PrivilegeDto {
    private UUID id;
    private String name;
    private String description;

    public PrivilegeDto() {
    }

    public PrivilegeDto(Privilege privilege) {
        this.id = privilege.getId();
        this.name = privilege.getName();
        this.description = privilege.getDescription();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
