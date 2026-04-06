package com.dev.LMS.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class AppRoleRequestDto {
    @NotBlank(message = "Role name is required")
    private String name;

    @NotBlank(message = "Role description is required")
    private String description;

    private Set<UUID> privilegeIds = new LinkedHashSet<>();

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

    public Set<UUID> getPrivilegeIds() {
        return privilegeIds;
    }

    public void setPrivilegeIds(Set<UUID> privilegeIds) {
        this.privilegeIds = privilegeIds;
    }
}
