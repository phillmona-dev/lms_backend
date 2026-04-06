package com.dev.LMS.service;

import com.dev.LMS.dto.BureauDashboardDto;
import com.dev.LMS.dto.SchoolPerformanceDto;
import com.dev.LMS.dto.StudentProgressSummaryDto;
import com.dev.LMS.model.Course;
import com.dev.LMS.model.Instructor;
import com.dev.LMS.model.Role;
import com.dev.LMS.model.School;
import com.dev.LMS.model.Student;
import com.dev.LMS.model.User;
import com.dev.LMS.repository.CourseRepository;
import com.dev.LMS.repository.SchoolRepository;
import com.dev.LMS.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class BureauAnalyticsService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final CourseRepository courseRepository;
    private final ProgressMonitoringService progressMonitoringService;

    public BureauAnalyticsService(UserService userService,
                                  UserRepository userRepository,
                                  SchoolRepository schoolRepository,
                                  CourseRepository courseRepository,
                                  ProgressMonitoringService progressMonitoringService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.courseRepository = courseRepository;
        this.progressMonitoringService = progressMonitoringService;
    }

    public BureauDashboardDto getDashboard(String email) {
        ensureBureauUser(email);
        List<SchoolPerformanceDto> schools = getSchoolPerformance(email);
        return new BureauDashboardDto(
                schools.size(),
                schools.stream().mapToLong(SchoolPerformanceDto::getStudentCount).sum(),
                schools.stream().mapToLong(SchoolPerformanceDto::getTeacherCount).sum(),
                schools.stream().mapToLong(SchoolPerformanceDto::getCourseCount).sum(),
                average(schools.stream().mapToDouble(SchoolPerformanceDto::getAverageAttendanceRate).toArray()),
                average(schools.stream().mapToDouble(SchoolPerformanceDto::getAverageAssignmentCompletionRate).toArray()),
                average(schools.stream().mapToDouble(SchoolPerformanceDto::getAverageOverallProgressRate).toArray()),
                schools.stream().mapToLong(SchoolPerformanceDto::getBehaviorReports).sum(),
                schools.stream().mapToLong(SchoolPerformanceDto::getHarmfulContentReports).sum(),
                schools.stream().mapToLong(SchoolPerformanceDto::getAtRiskStudents).sum()
        );
    }

    public List<SchoolPerformanceDto> getSchoolPerformance(String email) {
        ensureBureauUser(email);
        return schoolRepository.findAll().stream()
                .map(this::buildSchoolPerformance)
                .sorted(Comparator.comparing(SchoolPerformanceDto::getAverageOverallProgressRate).reversed())
                .toList();
    }

    public List<SchoolPerformanceDto> getHighRiskSchools(String email) {
        ensureBureauUser(email);
        return schoolRepository.findAll().stream()
                .map(this::buildSchoolPerformance)
                .filter(school -> school.getAtRiskStudents() > 0
                        || school.getHarmfulContentReports() > 0
                        || school.getAverageAttendanceRate() < 60.0)
                .sorted(Comparator.comparingLong(SchoolPerformanceDto::getAtRiskStudents).reversed()
                        .thenComparingLong(SchoolPerformanceDto::getHarmfulContentReports).reversed())
                .toList();
    }

    private SchoolPerformanceDto buildSchoolPerformance(School school) {
        List<User> schoolUsers = userRepository.findAll().stream()
                .filter(user -> user.getSchool() != null && school.getId().equals(user.getSchool().getId()))
                .toList();
        List<Course> schoolCourses = courseRepository.findAll().stream()
                .filter(course -> course.getSchool() != null && school.getId().equals(course.getSchool().getId()))
                .toList();
        List<StudentProgressSummaryDto> studentSummaries = schoolUsers.stream()
                .filter(user -> user instanceof Student)
                .map(user -> progressMonitoringService.getStudentProgress(user.getPublicId(), user.getPublicId()))
                .toList();

        return new SchoolPerformanceDto(
                school.getId(),
                school.getName(),
                school.getRegion(),
                schoolUsers.stream().filter(user -> user instanceof Student).count(),
                schoolUsers.stream().filter(user -> user instanceof Instructor).count(),
                schoolCourses.size(),
                average(studentSummaries.stream().mapToDouble(StudentProgressSummaryDto::getAttendanceRate).toArray()),
                average(studentSummaries.stream().mapToDouble(StudentProgressSummaryDto::getAssignmentCompletionRate).toArray()),
                average(studentSummaries.stream().mapToDouble(StudentProgressSummaryDto::getOverallProgressRate).toArray()),
                studentSummaries.stream().mapToLong(StudentProgressSummaryDto::getBehaviorReportCount).sum(),
                studentSummaries.stream().mapToLong(StudentProgressSummaryDto::getHarmfulContentReportCount).sum(),
                studentSummaries.stream().filter(this::isAtRisk).count()
        );
    }

    private void ensureBureauUser(String email) {
        User user = userService.getUserByEmail(email);
        Role role = user.getRole() == null ? null : user.getRole().canonical();
        if (role != Role.BUREAU_OF_EDUCATION && role != Role.SYSTEM_ADMINISTRATOR) {
            throw new IllegalStateException("Only bureau or system administrators can access regional analytics.");
        }
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
