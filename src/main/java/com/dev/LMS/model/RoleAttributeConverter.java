package com.dev.LMS.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RoleAttributeConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }

        return switch (role.canonical()) {
            case TEACHER -> Role.INSTRUCTOR.name();
            case SYSTEM_ADMINISTRATOR -> Role.ADMIN.name();
            default -> role.canonical().name();
        };
    }

    @Override
    public Role convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) {
            return null;
        }

        return Role.from(dbValue);
    }
}
