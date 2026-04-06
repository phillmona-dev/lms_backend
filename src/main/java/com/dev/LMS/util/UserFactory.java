package com.dev.LMS.util;


import com.dev.LMS.model.GenericUser;
import com.dev.LMS.model.User;
import org.springframework.stereotype.Component;
import com.dev.LMS.model.Role;
import com.dev.LMS.model.Admin;
import com.dev.LMS.model.Instructor;
import com.dev.LMS.model.Student;
@Component
public class UserFactory {
    public User createUser(String role, String name, String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        Role userRole = parseRole(role);
        return switch (userRole) {
            case STUDENT -> new Student(name, email);
            case TEACHER -> new Instructor(name, email);
            case SYSTEM_ADMINISTRATOR -> new Admin(name, email);
            case PARENT, SCHOOL_ADMINISTRATOR, BUREAU_OF_EDUCATION, AI_SYSTEM, AUTHENTICATION_SYSTEM ->
                    new GenericUser(name, email, userRole);
            default -> throw new RuntimeException("Unsupported role.");
        };
    }

    public User tempLoginUser(String role, String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        Role userRole = parseRole(role);
        return switch (userRole) {
            case STUDENT -> new Student("temp", email);
            case TEACHER -> new Instructor("temp", email);
            case SYSTEM_ADMINISTRATOR -> new Admin("temp", email);
            case PARENT, SCHOOL_ADMINISTRATOR, BUREAU_OF_EDUCATION, AI_SYSTEM, AUTHENTICATION_SYSTEM ->
                    new GenericUser("temp", email, userRole);
            default -> throw new RuntimeException("Unsupported role.");
        };
    }

    private Role parseRole(String role) {
        try {
            return Role.from(role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Must be one of: " + Role.supportedRoles());
        }
    }
}
