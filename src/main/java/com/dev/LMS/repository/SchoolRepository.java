package com.dev.LMS.repository;

import com.dev.LMS.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findByNameIgnoreCase(String name);
    List<School> findByRegionIgnoreCase(String region);
}
