package com.dev.LMS.repository;

import com.dev.LMS.model.Course;
import com.dev.LMS.model.LiveSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {
    Optional<LiveSession> findFirstByCourseAndActiveTrueOrderByStartedAtDesc(Course course);
    List<LiveSession> findByCourseOrderByStartedAtDesc(Course course);
}
