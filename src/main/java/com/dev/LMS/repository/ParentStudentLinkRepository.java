package com.dev.LMS.repository;

import com.dev.LMS.model.ParentStudentLink;
import com.dev.LMS.model.Student;
import com.dev.LMS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, Long> {
    List<ParentStudentLink> findByParentAndActiveTrue(User parent);
    List<ParentStudentLink> findByStudentAndActiveTrue(Student student);
    Optional<ParentStudentLink> findByParentAndStudent(User parent, Student student);
    boolean existsByParentAndStudentAndActiveTrue(User parent, Student student);
}
