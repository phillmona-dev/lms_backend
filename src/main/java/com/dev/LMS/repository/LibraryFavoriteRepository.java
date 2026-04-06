package com.dev.LMS.repository;

import com.dev.LMS.model.LibraryFavorite;
import com.dev.LMS.model.LibraryResource;
import com.dev.LMS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryFavoriteRepository extends JpaRepository<LibraryFavorite, Long> {
    List<LibraryFavorite> findByUserOrderByCreatedAtDesc(User user);
    Optional<LibraryFavorite> findByUserAndResource(User user, LibraryResource resource);
    long countByResource(LibraryResource resource);
    long countByUser(User user);
}
