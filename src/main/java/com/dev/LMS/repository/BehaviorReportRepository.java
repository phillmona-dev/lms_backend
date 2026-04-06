package com.dev.LMS.repository;

import com.dev.LMS.model.BehaviorReport;
import com.dev.LMS.model.Instructor;
import com.dev.LMS.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BehaviorReportRepository extends JpaRepository<BehaviorReport, Long> {
    List<BehaviorReport> findByStudentOrderByCreatedAtDesc(Student student);
    List<BehaviorReport> findByTeacherOrderByCreatedAtDesc(Instructor teacher);
    long countByStudent(Student student);
}
