package com.dev.LMS.service;

import com.dev.LMS.dto.AnnouncementDto;
import com.dev.LMS.dto.AnnouncementRequestDto;
import com.dev.LMS.dto.SchoolAdminDashboardDto;
import com.dev.LMS.dto.StudentProgressSummaryDto;
import com.dev.LMS.model.Announcement;
import com.dev.LMS.model.Course;
import com.dev.LMS.model.Instructor;
import com.dev.LMS.model.Role;
import com.dev.LMS.model.Student;
import com.dev.LMS.model.User;
import com.dev.LMS.repository.AnnouncementRepository;
import com.dev.LMS.repository.BehaviorReportRepository;
import com.dev.LMS.repository.CourseRepository;
import com.dev.LMS.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolAdministrationService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final BehaviorReportRepository behaviorReportRepository;
    private final ProgressMonitoringService progressMonitoringService;
    private final AnnouncementRepository announcementRepository;
    private final NotificationService notificationService;

    public SchoolAdministrationService(UserService userService,
                                       UserRepository userRepository,
                                       CourseRepository courseRepository,
                                       BehaviorReportRepository behaviorReportRepository,
                                       ProgressMonitoringService progressMonitoringService,
                                       AnnouncementRepository announcementRepository,
                                       NotificationService notificationService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.behaviorReportRepository = behaviorReportRepository;
        this.progressMonitoringService = progressMonitoringService;
        this.announcementRepository = announcementRepository;
        this.notificationService = notificationService;
    }

    public SchoolAdminDashboardDto getDashboard(String email) {
        ensureSchoolAdmin(email);

        List<User> users = userRepository.findAll();
        List<Course> courses = courseRepository.findAll();
        List<StudentProgressSummaryDto> studentProgress = users.stream()
                .filter(user -> user instanceof Student)
                .map(user -> progressMonitoringService.getStudentProgress(user.getPublicId(), user.getPublicId()))
                .toList();

        long totalStudents = users.stream().filter(user -> user instanceof Student).count();
        long totalTeachers = users.stream().filter(user -> user instanceof Instructor).count();
        long totalParents = users.stream()
                .filter(user -> user.getRole() != null && user.getRole().canonical() == Role.PARENT)
                .count();
        long totalEnrollments = courses.stream().mapToLong(course -> course.getEnrolled_students().size()).sum();
        double averageAttendance = average(studentProgress.stream().mapToDouble(StudentProgressSummaryDto::getAttendanceRate).toArray());
        double averageAssignmentCompletion = average(studentProgress.stream().mapToDouble(StudentProgressSummaryDto::getAssignmentCompletionRate).toArray());
        double averageOverallProgress = average(studentProgress.stream().mapToDouble(StudentProgressSummaryDto::getOverallProgressRate).toArray());
        long totalBehaviorReports = studentProgress.stream().mapToLong(StudentProgressSummaryDto::getBehaviorReportCount).sum();
        long harmfulContentReports = studentProgress.stream().mapToLong(StudentProgressSummaryDto::getHarmfulContentReportCount).sum();
        long studentsAtRisk = studentProgress.stream().filter(this::isAtRisk).count();

        return new SchoolAdminDashboardDto(
                users.size(),
                totalStudents,
                totalTeachers,
                totalParents,
                courses.size(),
                totalEnrollments,
                averageAttendance,
                averageAssignmentCompletion,
                averageOverallProgress,
                totalBehaviorReports,
                harmfulContentReports,
                studentsAtRisk
        );
    }

    public AnnouncementDto publishAnnouncement(String email, AnnouncementRequestDto requestDto) {
        User admin = ensureSchoolAdmin(email);
        Announcement announcement = new Announcement();
        announcement.setTitle(requestDto.getTitle().trim());
        announcement.setMessage(requestDto.getMessage().trim());
        announcement.setAudience(requestDto.getAudience().trim().toUpperCase());
        announcement.setCreatedBy(admin);
        Announcement saved = announcementRepository.save(announcement);

        String notificationText = "Announcement: " + saved.getTitle();
        for (User user : userRepository.findAll()) {
            if (user instanceof Student student && matchesAudience(saved.getAudience(), user)) {
                notificationService.addNotifcationStudent(notificationService.createNotification(notificationText), student);
            }
            if (user instanceof Instructor instructor && matchesAudience(saved.getAudience(), user)) {
                notificationService.addNotificationInstructor(notificationService.createNotification(notificationText), instructor);
            }
        }

        return new AnnouncementDto(saved);
    }

    public List<AnnouncementDto> getAnnouncements(String email) {
        User viewer = ensureAuthenticated(email);
        return announcementRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(announcement -> matchesAudience(announcement.getAudience(), viewer))
                .map(AnnouncementDto::new)
                .toList();
    }

    public List<StudentProgressSummaryDto> getStudentProgressReport(String email) {
        ensureSchoolAdmin(email);
        return userRepository.findAll().stream()
                .filter(user -> user instanceof Student)
                .map(user -> progressMonitoringService.getStudentProgress(user.getPublicId(), user.getPublicId()))
                .toList();
    }

    private User ensureSchoolAdmin(String email) {
        User user = userService.getUserByEmail(email);
        if (user.getRole() == null || user.getRole().canonical() != Role.SCHOOL_ADMINISTRATOR) {
            throw new IllegalStateException("Only school administrators can access this endpoint.");
        }
        return user;
    }

    private User ensureAuthenticated(String email) {
        return userService.getUserByEmail(email);
    }

    private boolean matchesAudience(String audience, User user) {
        if ("ALL".equals(audience)) {
            return true;
        }
        Role role = user.getRole() == null ? null : user.getRole().canonical();
        return role != null && role.name().equalsIgnoreCase(audience);
    }

    private double average(double[] values) {
        if (values.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return Math.round((sum / values.length) * 100.0) / 100.0;
    }

    private boolean isAtRisk(StudentProgressSummaryDto summary) {
        return summary.getAttendanceRate() < 50.0
                || summary.getAssignmentCompletionRate() < 50.0
                || summary.getHarmfulContentReportCount() > 0
                || summary.getBehaviorReportCount() >= 3;
    }
}
