package com.dev.LMS.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class PrivilegeIdsRequestDto {
    @NotEmpty(message = "privilegeIds must not be empty")
    private Set<UUID> privilegeIds = new LinkedHashSet<>();

    public Set<UUID> getPrivilegeIds() {
        return privilegeIds;
    }

    public void setPrivilegeIds(Set<UUID> privilegeIds) {
        this.privilegeIds = privilegeIds;
    }
}
