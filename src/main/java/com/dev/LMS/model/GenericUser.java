package com.dev.LMS.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "generic_users")
public class GenericUser extends User {
    public GenericUser() {
    }

    public GenericUser(String name, String email, Role role) {
        super(name, email, role.canonical());
    }
}
