package com.dev.LMS.service;

import com.dev.LMS.dto.BehaviorReportDto;
import com.dev.LMS.dto.BehaviorReportRequestDto;
import com.dev.LMS.model.BehaviorReport;
import com.dev.LMS.model.Course;
import com.dev.LMS.model.Instructor;
import com.dev.LMS.model.Role;
import com.dev.LMS.model.Student;
import com.dev.LMS.model.User;
import com.dev.LMS.repository.BehaviorReportRepository;
import com.dev.LMS.repository.ParentStudentLinkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BehaviorReportService {
    private final BehaviorReportRepository behaviorReportRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final UserService userService;
    private final CourseService courseService;
    private final NotificationService notificationService;

    public BehaviorReportService(BehaviorReportRepository behaviorReportRepository,
                                 ParentStudentLinkRepository parentStudentLinkRepository,
                                 UserService userService,
                                 CourseService courseService,
                                 NotificationService notificationService) {
        this.behaviorReportRepository = behaviorReportRepository;
        this.parentStudentLinkRepository = parentStudentLinkRepository;
        this.userService = userService;
        this.courseService = courseService;
        this.notificationService = notificationService;
    }

    public BehaviorReportDto createReport(String teacherEmail, String courseName, UUID studentId, BehaviorReportRequestDto requestDto) {
        User requester = userService.getUserByEmail(teacherEmail);
        if (!(requester instanceof Instructor teacher)) {
            throw new IllegalStateException("Only teachers can create behavior reports.");
        }

        Course course = courseService.getCourse(courseName);
        if (course == null) {
            throw new IllegalStateException("Course not found.");
        }
        if (course.getInstructor() == null || course.getInstructor().getId() != teacher.getId()) {
            throw new IllegalStateException("You are not allowed to report behavior for this course.");
        }

        User studentUser = userService.getUserByPublicId(studentId);
        if (!(studentUser instanceof Student student)) {
            throw new IllegalStateException("Student not found.");
        }
        if (!course.getEnrolled_students().contains(student)) {
            throw new IllegalStateException("Student is not enrolled in this course.");
        }

        BehaviorReport report = new BehaviorReport();
        report.setTeacher(teacher);
        report.setStudent(student);
        report.setCourse(course);
        report.setTitle(requestDto.getTitle().trim());
        report.setDescription(requestDto.getDescription().trim());
        report.setSeverity(requestDto.getSeverity());
        report.setHarmfulContent(requestDto.isHarmfulContent());
        report.setActionTaken(requestDto.getActionTaken() == null ? null : requestDto.getActionTaken().trim());

        BehaviorReport savedReport = behaviorReportRepository.save(report);
        notificationService.addNotifcationStudent(
                notificationService.createNotification(buildNotificationMessage(savedReport)),
                student
        );
        return new BehaviorReportDto(savedReport);
    }

    public List<BehaviorReportDto> getTeacherReports(String teacherEmail) {
        User requester = userService.getUserByEmail(teacherEmail);
        if (!(requester instanceof Instructor teacher)) {
            throw new IllegalStateException("Only teachers can view their reported issues.");
        }
        return behaviorReportRepository.findByTeacherOrderByCreatedAtDesc(teacher).stream()
                .map(BehaviorReportDto::new)
                .toList();
    }

    public List<BehaviorReportDto> getStudentReports(String requesterEmail, UUID studentId) {
        User requester = userService.getUserByEmail(requesterEmail);
        User studentUser = userService.getUserByPublicId(studentId);
        if (!(studentUser instanceof Student student)) {
            throw new IllegalStateException("Student not found.");
        }
        if (!canViewStudentReports(requester, student)) {
            throw new IllegalStateException("You are not authorized to view behavior reports for this student.");
        }
        return behaviorReportRepository.findByStudentOrderByCreatedAtDesc(student).stream()
                .map(BehaviorReportDto::new)
                .toList();
    }

    private boolean canViewStudentReports(User requester, Student student) {
        Role role = requester.getRole() == null ? null : requester.getRole().canonical();
        if (role == Role.SYSTEM_ADMINISTRATOR || role == Role.SCHOOL_ADMINISTRATOR) {
            return true;
        }
        if (role == Role.PARENT) {
            return parentStudentLinkRepository.existsByParentAndStudentAndActiveTrue(requester, student);
        }
        if (role == Role.TEACHER && requester instanceof Instructor teacher) {
            return teacher.getCreatedCourses().stream()
                    .anyMatch(course -> course.getEnrolled_students().contains(student));
        }
        if (role == Role.STUDENT) {
            return requester.getPublicId().equals(student.getPublicId());
        }
        return false;
    }

    private String buildNotificationMessage(BehaviorReport report) {
        String prefix = report.isHarmfulContent() ? "Harmful content warning" : "Behavior report";
        return prefix + ": " + report.getTitle() + " in course " + report.getCourse().getName();
    }
}
