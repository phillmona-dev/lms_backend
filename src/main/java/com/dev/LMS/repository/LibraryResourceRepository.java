package com.dev.LMS.repository;

import com.dev.LMS.model.LibraryResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryResourceRepository extends JpaRepository<LibraryResource, Long> {
}
