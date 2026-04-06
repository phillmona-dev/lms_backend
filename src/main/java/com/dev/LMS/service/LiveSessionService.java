package com.dev.LMS.service;

import com.dev.LMS.dto.LiveSessionDto;
import com.dev.LMS.dto.LiveSessionActivityRequest;
import com.dev.LMS.model.Course;
import com.dev.LMS.model.Instructor;
import com.dev.LMS.model.LiveSession;
import com.dev.LMS.model.Notification;
import com.dev.LMS.model.Student;
import com.dev.LMS.model.User;
import com.dev.LMS.repository.LiveSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class LiveSessionService {
    private static final Logger log = LoggerFactory.getLogger(LiveSessionService.class);

    private final CourseService courseService;
    private final UserService userService;
    private final LiveSessionRepository liveSessionRepository;
    private final NotificationService notificationService;

    public LiveSessionService(CourseService courseService,
                              UserService userService,
                              LiveSessionRepository liveSessionRepository,
                              NotificationService notificationService) {
        this.courseService = courseService;
        this.userService = userService;
        this.liveSessionRepository = liveSessionRepository;
        this.notificationService = notificationService;
    }

    public LiveSessionDto startSession(String courseName, String requesterEmail, String topic) {
        Course course = getCourseOrThrow(courseName);
        Instructor instructor = getCourseInstructorOrThrow(course, requesterEmail);

        LiveSession activeSession = liveSessionRepository
                .findFirstByCourseAndActiveTrueOrderByStartedAtDesc(course)
                .orElse(null);
        if (activeSession != null) {
            return new LiveSessionDto(activeSession);
        }

        String normalizedTopic = topic == null || topic.isBlank()
                ? "Live Class - " + course.getName()
                : topic.trim();

        LiveSession session = new LiveSession();
        String roomName = buildRoomName(course.getName());
        session.setRoomName(roomName);
        session.setJoinUrl("https://meet.jit.si/" + roomName);
        session.setTopic(normalizedTopic);
        session.setActive(true);
        session.setStartedAt(LocalDateTime.now());
        session.setCourse(course);
        session.setStartedBy(instructor);

        return new LiveSessionDto(liveSessionRepository.save(session));
    }

    public LiveSessionDto endSession(String courseName, Long sessionId, String requesterEmail) {
        Course course = getCourseOrThrow(courseName);
        getCourseInstructorOrThrow(course, requesterEmail);

        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("Live session not found."));
        if (session.getCourse() == null || session.getCourse().getCourseId() != course.getCourseId()) {
            throw new IllegalStateException("Session does not belong to this course.");
        }

        session.setActive(false);
        session.setEndedAt(LocalDateTime.now());
        return new LiveSessionDto(liveSessionRepository.save(session));
    }

    public LiveSessionDto getActiveSession(String courseName, String requesterEmail) {
        Course course = getCourseOrThrow(courseName);
        ensureCourseParticipant(course, requesterEmail);

        LiveSession session = liveSessionRepository
                .findFirstByCourseAndActiveTrueOrderByStartedAtDesc(course)
                .orElseThrow(() -> new IllegalStateException("No active live session for this course right now."));
        return new LiveSessionDto(session);
    }

    public List<LiveSessionDto> getSessions(String courseName, String requesterEmail) {
        Course course = getCourseOrThrow(courseName);
        ensureCourseParticipant(course, requesterEmail);
        return liveSessionRepository.findByCourseOrderByStartedAtDesc(course).stream()
                .map(LiveSessionDto::new)
                .toList();
    }

    public void reportStudentActivity(String courseName,
                                      Long sessionId,
                                      String requesterEmail,
                                      LiveSessionActivityRequest request,
                                      String clientIp) {
        Course course = getCourseOrThrow(courseName);
        User user = userService.getUserByEmail(requesterEmail);
        if (!(user instanceof Student student)) {
            throw new AccessDeniedException("Only students can report live session activity.");
        }
        if (!student.getEnrolled_courses().contains(course)) {
            throw new AccessDeniedException("You are not enrolled in this course.");
        }

        LiveSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("Live session not found."));
        if (session.getCourse() == null || session.getCourse().getCourseId() != course.getCourseId()) {
            throw new IllegalStateException("Session does not belong to this course.");
        }
        if (!session.isActive()) {
            return;
        }

        Instructor teacher = course.getInstructor();
        if (teacher == null) {
            return;
        }

        String page = summarizeValue(request.getPageUrl(), 70);
        String title = summarizeValue(request.getPageTitle(), 45);
        String eventType = summarizeValue(request.getEventType(), 28);
        String visibility = summarizeValue(request.getVisibilityState(), 16);
        String timezone = summarizeValue(request.getTimezone(), 32);
        String platform = summarizeValue(request.getPlatform(), 22);

        String message = "[Live Session Activity] " +
                student.getName() + " (" + student.getEmail() + ") " +
                "sid=" + sessionId +
                ", " +
                "course=" + summarizeValue(course.getName(), 36) +
                ", " +
                "event=" + eventType +
                ", page=" + page +
                ", title=" + title +
                ", vis=" + visibility +
                ", tz=" + timezone +
                ", platform=" + platform +
                ", ip=" + safeValue(clientIp, "unknown");

        Notification notification = notificationService.createNotification(message);
        notificationService.addNotificationInstructor(notification, teacher);
        log.info("Live activity alert stored for teacher='{}' from student='{}' event='{}' course='{}' sessionId={}",
                teacher.getEmail(),
                student.getEmail(),
                safeValue(request.getEventType(), "unknown"),
                course.getName(),
                sessionId);
    }

    private Course getCourseOrThrow(String courseName) {
        Course course = courseService.getCourse(courseName);
        if (course == null) {
            throw new IllegalStateException("Course not found.");
        }
        return course;
    }

    private Instructor getCourseInstructorOrThrow(Course course, String requesterEmail) {
        User user = userService.getUserByEmail(requesterEmail);
        if (!(user instanceof Instructor instructor)) {
            throw new AccessDeniedException("Only teachers can start or end live sessions.");
        }
        if (course.getInstructor() == null || course.getInstructor().getId() != instructor.getId()) {
            throw new AccessDeniedException("You can only manage live sessions for your own course.");
        }
        return instructor;
    }

    private void ensureCourseParticipant(Course course, String requesterEmail) {
        User user = userService.getUserByEmail(requesterEmail);
        if (user instanceof Instructor instructor) {
            if (course.getInstructor() != null && course.getInstructor().getId() == instructor.getId()) {
                return;
            }
        }
        if (user instanceof Student student && student.getEnrolled_courses().contains(course)) {
            return;
        }
        throw new AccessDeniedException("You are not allowed to access live sessions for this course.");
    }

    private String buildRoomName(String courseName) {
        String slug = courseName
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
        if (slug.isBlank()) {
            slug = "course";
        }
        return "lms-afrinode-" + slug + "-" + System.currentTimeMillis();
    }

    private String safeValue(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String summarizeValue(String value, int maxLength) {
        String safe = safeValue(value, "unknown");
        if (safe.length() <= maxLength) {
            return safe;
        }
        return safe.substring(0, Math.max(0, maxLength - 3)) + "...";
    }
}
