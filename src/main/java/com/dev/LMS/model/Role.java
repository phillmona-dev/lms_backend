package com.dev.LMS.model;

public enum Role {
    STUDENT,
    TEACHER,
    PARENT,
    SCHOOL_ADMINISTRATOR,
    BUREAU_OF_EDUCATION,
    AI_SYSTEM,
    SYSTEM_ADMINISTRATOR,
    AUTHENTICATION_SYSTEM,
    ADMIN,
    INSTRUCTOR;

    public static Role from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }
        Role parsedRole = Role.valueOf(value.trim().toUpperCase());
        return parsedRole.canonical();
    }

    public Role canonical() {
        return switch (this) {
            case ADMIN -> SYSTEM_ADMINISTRATOR;
            case INSTRUCTOR -> TEACHER;
            default -> this;
        };
    }

    public boolean isAdministrativeRole() {
        Role canonicalRole = canonical();
        return canonicalRole == SCHOOL_ADMINISTRATOR || canonicalRole == SYSTEM_ADMINISTRATOR;
    }

    public static String supportedRoles() {
        return String.join(", ",
                STUDENT.name(),
                TEACHER.name(),
                PARENT.name(),
                SCHOOL_ADMINISTRATOR.name(),
                BUREAU_OF_EDUCATION.name(),
                AI_SYSTEM.name(),
                SYSTEM_ADMINISTRATOR.name(),
                AUTHENTICATION_SYSTEM.name());
    }
}
