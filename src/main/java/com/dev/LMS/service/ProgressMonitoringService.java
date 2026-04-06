package com.dev.LMS.service;

import com.dev.LMS.dto.CourseProgressDto;
import com.dev.LMS.dto.ParentStudentLinkDto;
import com.dev.LMS.dto.StudentProgressSummaryDto;
import com.dev.LMS.model.AssignmentSubmission;
import com.dev.LMS.model.BehaviorReport;
import com.dev.LMS.model.Course;
import com.dev.LMS.model.ParentStudentLink;
import com.dev.LMS.model.QuizSubmission;
import com.dev.LMS.model.Role;
import com.dev.LMS.model.Student;
import com.dev.LMS.model.User;
import com.dev.LMS.repository.BehaviorReportRepository;
import com.dev.LMS.repository.ParentStudentLinkRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ProgressMonitoringService {
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final BehaviorReportRepository behaviorReportRepository;
    private final UserService userService;

    public ProgressMonitoringService(ParentStudentLinkRepository parentStudentLinkRepository,
                                     BehaviorReportRepository behaviorReportRepository,
                                     UserService userService) {
        this.parentStudentLinkRepository = parentStudentLinkRepository;
        this.behaviorReportRepository = behaviorReportRepository;
        this.userService = userService;
    }

    public ParentStudentLinkDto linkParentToStudent(UUID parentId, UUID studentId) {
        User parent = userService.getUserByPublicId(parentId);
        User studentUser = userService.getUserByPublicId(studentId);

        if (parent.getRole() == null || parent.getRole().canonical() != Role.PARENT) {
            throw new IllegalStateException("Selected parent user does not have the PARENT role.");
        }
        if (!(studentUser instanceof Student student)) {
            throw new IllegalStateException("Selected student user is not a student.");
        }

        ParentStudentLink link = parentStudentLinkRepository.findByParentAndStudent(parent, student)
                .orElseGet(() -> new ParentStudentLink(parent, student));
        link.setActive(true);
        return new ParentStudentLinkDto(parentStudentLinkRepository.save(link));
    }

    public void unlinkParentFromStudent(UUID parentId, UUID studentId) {
        User parent = userService.getUserByPublicId(parentId);
        User studentUser = userService.getUserByPublicId(studentId);
        if (!(studentUser instanceof Student student)) {
            throw new IllegalStateException("Selected student user is not a student.");
        }
        ParentStudentLink link = parentStudentLinkRepository.findByParentAndStudent(parent, student)
                .orElseThrow(() -> new IllegalStateException("Parent-student link not found."));
        link.setActive(false);
        parentStudentLinkRepository.save(link);
    }

    public List<ParentStudentLinkDto> getParentLinks(UUID parentId) {
        User parent = userService.getUserByPublicId(parentId);
        ensureParentUser(parent);
        return parentStudentLinkRepository.findByParentAndActiveTrue(parent).stream()
                .map(ParentStudentLinkDto::new)
                .toList();
    }

    public List<StudentProgressSummaryDto> getLinkedStudentProgress(UUID parentId) {
        User parent = userService.getUserByPublicId(parentId);
        ensureParentUser(parent);
        return parentStudentLinkRepository.findByParentAndActiveTrue(parent).stream()
                .map(ParentStudentLink::getStudent)
                .map(this::buildStudentProgress)
                .toList();
    }

    public StudentProgressSummaryDto getStudentProgress(UUID requesterId, UUID studentId) {
        User requester = userService.getUserByPublicId(requesterId);
        User studentUser = userService.getUserByPublicId(studentId);
        if (!(studentUser instanceof Student student)) {
            throw new IllegalStateException("Student not found.");
        }
        if (!canViewStudentProgress(requester, student)) {
            throw new IllegalStateException("You are not authorized to view this student progress.");
        }
        return buildStudentProgress(student);
    }

    public StudentProgressSummaryDto getMyProgress(String email) {
        User user = userService.getUserByEmail(email);
        if (!(user instanceof Student student)) {
            throw new IllegalStateException("Only students can view personal progress.");
        }
        return buildStudentProgress(student);
    }

    public List<StudentProgressSummaryDto> getTeacherStudentProgress(String email) {
        User user = userService.getUserByEmail(email);
        if (user.getRole() == null || user.getRole().canonical() != Role.TEACHER) {
            throw new IllegalStateException("Only teachers can view their learners progress.");
        }
        Set<Student> students = new LinkedHashSet<>();
        for (Course course : getCoursesForUser(user)) {
            students.addAll(course.getEnrolled_students());
        }
        return students.stream()
                .sorted(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::buildStudentProgress)
                .toList();
    }

    private boolean canViewStudentProgress(User requester, Student student) {
        Role requesterRole = requester.getRole() == null ? null : requester.getRole().canonical();
        if (requester.getPublicId().equals(student.getPublicId())) {
            return true;
        }
        if (requesterRole == Role.SYSTEM_ADMINISTRATOR || requesterRole == Role.SCHOOL_ADMINISTRATOR) {
            return true;
        }
        if (requesterRole == Role.PARENT) {
            return parentStudentLinkRepository.existsByParentAndStudentAndActiveTrue(requester, student);
        }
        if (requesterRole == Role.TEACHER) {
            for (Course course : getCoursesForUser(requester)) {
                if (course.getEnrolled_students().contains(student)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void ensureParentUser(User user) {
        if (user.getRole() == null || user.getRole().canonical() != Role.PARENT) {
            throw new IllegalStateException("Only parent users can access linked-student views.");
        }
    }

    private Set<Course> getCoursesForUser(User user) {
        if (user instanceof Student student) {
            return student.getEnrolled_courses();
        }
        if (user instanceof com.dev.LMS.model.Instructor instructor) {
            return instructor.getCreatedCourses();
        }
        return Set.of();
    }

    private StudentProgressSummaryDto buildStudentProgress(Student student) {
        Set<Course> courses = student.getEnrolled_courses();
        int totalLessons = 0;
        int attendedLessons = 0;
        int totalAssignments = 0;
        int submittedAssignments = 0;
        int gradedAssignments = 0;
        int totalQuizzes = 0;
        int quizAttempts = 0;
        int quizGradeSum = 0;
        int assignmentGradeSum = 0;
        List<CourseProgressDto> courseProgress = new ArrayList<>();

        for (Course course : courses) {
            int courseTotalLessons = course.getLessons().size();
            int courseAttendedLessons = (int) student.getLessonAttended().stream()
                    .filter(lesson -> lesson.getCourse() != null && lesson.getCourse().equals(course))
                    .count();

            int courseTotalAssignments = course.getAssignments().size();
            int courseSubmittedAssignments = (int) student.getAssignmentSubmissions().stream()
                    .filter(submission -> submission.getAssignment() != null
                            && submission.getAssignment().getCourse() != null
                            && submission.getAssignment().getCourse().equals(course))
                    .count();

            int courseTotalQuizzes = course.getQuizzes().size();
            int courseAttemptedQuizzes = (int) student.getQuizSubmissions().stream()
                    .filter(submission -> submission.getQuiz() != null
                            && submission.getQuiz().getCourse() != null
                            && submission.getQuiz().getCourse().equals(course))
                    .count();

            totalLessons += courseTotalLessons;
            attendedLessons += courseAttendedLessons;
            totalAssignments += courseTotalAssignments;
            submittedAssignments += courseSubmittedAssignments;
            totalQuizzes += courseTotalQuizzes;
            quizAttempts += courseAttemptedQuizzes;

            courseProgress.add(new CourseProgressDto(
                    course,
                    courseTotalLessons,
                    courseAttendedLessons,
                    courseTotalAssignments,
                    courseSubmittedAssignments,
                    courseTotalQuizzes,
                    courseAttemptedQuizzes
            ));
        }

        for (AssignmentSubmission submission : student.getAssignmentSubmissions()) {
            if (submission.isGraded()) {
                gradedAssignments++;
                assignmentGradeSum += submission.getGrade();
            }
        }

        for (QuizSubmission submission : student.getQuizSubmissions()) {
            quizGradeSum += submission.getGrade();
        }

        int unreadAlerts = (int) student.getNotifications().stream()
                .filter(notification -> !notification.isRead())
                .count();
        List<BehaviorReport> behaviorReports = behaviorReportRepository.findByStudentOrderByCreatedAtDesc(student);
        long harmfulContentReportCount = behaviorReports.stream()
                .filter(BehaviorReport::isHarmfulContent)
                .count();

        double attendanceRate = toPercentage(attendedLessons, totalLessons);
        double assignmentCompletionRate = toPercentage(submittedAssignments, totalAssignments);
        double quizParticipationRate = toPercentage(quizAttempts, totalQuizzes);
        double overallProgressRate = round((attendanceRate + assignmentCompletionRate + quizParticipationRate) / 3.0);

        Double averageAssignmentGrade = gradedAssignments == 0 ? null : round(assignmentGradeSum / (double) gradedAssignments);
        Double averageQuizGrade = quizAttempts == 0 ? null : round(quizGradeSum / (double) quizAttempts);

        courseProgress.sort(Comparator.comparing(CourseProgressDto::getCourseName, String.CASE_INSENSITIVE_ORDER));

        return new StudentProgressSummaryDto(
                student.getPublicId(),
                student.getName(),
                student.getEmail(),
                courses.size(),
                totalLessons,
                attendedLessons,
                attendanceRate,
                totalAssignments,
                submittedAssignments,
                assignmentCompletionRate,
                gradedAssignments,
                averageAssignmentGrade,
                quizAttempts,
                averageQuizGrade,
                unreadAlerts,
                behaviorReports.size(),
                harmfulContentReportCount,
                overallProgressRate,
                courseProgress
        );
    }

    private double toPercentage(int completed, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return round((completed * 100.0) / total);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
