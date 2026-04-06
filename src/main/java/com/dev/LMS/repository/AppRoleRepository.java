package com.dev.LMS.repository;

import com.dev.LMS.model.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppRoleRepository extends JpaRepository<AppRole, UUID> {
    Optional<AppRole> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
