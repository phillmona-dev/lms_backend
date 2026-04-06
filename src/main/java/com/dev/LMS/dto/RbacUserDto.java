package com.dev.LMS.dto;

import com.dev.LMS.model.User;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RbacUserDto {
    private UUID id;
    private int legacyId;
    private String name;
    private String email;
    private String legacyRole;
    private Long schoolId;
    private String schoolName;
    private Set<AppRoleDto> roles;
    private Set<String> authorities;

    public RbacUserDto(User user) {
        this.id = user.getPublicId();
        this.legacyId = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.legacyRole = user.getRole() == null ? null : user.getRole().canonical().name();
        this.schoolId = user.getSchool() == null ? null : user.getSchool().getId();
        this.schoolName = user.getSchool() == null ? null : user.getSchool().getName();
        this.roles = user.getAccessRoles().stream()
                .map(AppRoleDto::new)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        this.authorities = user.getAuthorityNames();
    }

    public UUID getId() {
        return id;
    }

    public int getLegacyId() {
        return legacyId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getLegacyRole() {
        return legacyRole;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public Set<AppRoleDto> getRoles() {
        return roles;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }
}
