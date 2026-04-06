package com.dev.LMS.repository;

import com.dev.LMS.model.LibraryResource;
import com.dev.LMS.model.LibraryReview;
import com.dev.LMS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryReviewRepository extends JpaRepository<LibraryReview, Long> {
    List<LibraryReview> findByResourceOrderByUpdatedAtDesc(LibraryResource resource);
    Optional<LibraryReview> findByUserAndResource(User user, LibraryResource resource);
}
