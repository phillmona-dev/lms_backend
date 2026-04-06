package com.dev.LMS.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class RoleIdsRequestDto {
    @NotEmpty(message = "roleIds must not be empty")
    private Set<UUID> roleIds = new LinkedHashSet<>();

    public Set<UUID> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<UUID> roleIds) {
        this.roleIds = roleIds;
    }
}
