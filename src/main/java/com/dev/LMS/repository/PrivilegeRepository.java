package com.dev.LMS.repository;

import com.dev.LMS.model.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PrivilegeRepository extends JpaRepository<Privilege, UUID> {
    Optional<Privilege> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
