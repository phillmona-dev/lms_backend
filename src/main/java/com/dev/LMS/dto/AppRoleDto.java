package com.dev.LMS.dto;

import com.dev.LMS.model.AppRole;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AppRoleDto {
    private UUID id;
    private String name;
    private String description;
    private Set<PrivilegeDto> privileges;

    public AppRoleDto() {
    }

    public AppRoleDto(AppRole appRole) {
        this.id = appRole.getId();
        this.name = appRole.getName();
        this.description = appRole.getDescription();
        this.privileges = appRole.getPrivileges().stream()
                .map(PrivilegeDto::new)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
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

    public Set<PrivilegeDto> getPrivileges() {
        return privileges;
    }
}
