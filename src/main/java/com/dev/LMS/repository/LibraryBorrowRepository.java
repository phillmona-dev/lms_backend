package com.dev.LMS.repository;

import com.dev.LMS.model.LibraryBorrow;
import com.dev.LMS.model.LibraryBorrowStatus;
import com.dev.LMS.model.LibraryResource;
import com.dev.LMS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryBorrowRepository extends JpaRepository<LibraryBorrow, Long> {
    List<LibraryBorrow> findByUserOrderByBorrowedAtDesc(User user);
    List<LibraryBorrow> findByUserAndStatusOrderByBorrowedAtDesc(User user, LibraryBorrowStatus status);
    Optional<LibraryBorrow> findByUserAndResourceAndStatus(User user, LibraryResource resource, LibraryBorrowStatus status);
    long countByStatus(LibraryBorrowStatus status);
    long countByUser(User user);
}
