package com.dev.LMS.service;

import com.dev.LMS.dto.NotificationDto;
import com.dev.LMS.model.Instructor;
import com.dev.LMS.model.Notification;
import com.dev.LMS.model.Student;
import com.dev.LMS.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final int MAX_NOTIFICATION_MESSAGE_LENGTH = 255;
    private static final String LIVE_ACTIVITY_MARKER = "[Live Session Activity]";
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(String notificationMessage) {
        Notification notification = new Notification(normalizeMessage(notificationMessage), LocalDateTime.now(),false);
        return notification;
    }

    public void addNotifcationStudent(Notification notification, Student student) {
        student.addNotification(notification);
        notificationRepository.save(notification);
    }

    public void removeNotifcationStudent(Notification notification, Student student) {
        student.removeNotification(notification);
        notificationRepository.save(notification);
    }

    public void addNotificationInstructor(Notification notification, Instructor instructor) {
        instructor.addNotification(notification);
        notificationRepository.save(notification);
    }
    public void removeNotificationInstructor(Notification notification, Instructor instructor) {
        instructor.removeNotification(notification);
        notificationRepository.save(notification);
    }

    public List<NotificationDto> getStudentNotification(Student student) {
        List<Notification> notifications = student.getNotifications();
        List<NotificationDto> notificationDtos = new ArrayList<>();
        for (Notification notification : notifications) {
            notificationDtos.add(new NotificationDto(notification));
        }
        return notificationDtos;
    }

    public List<NotificationDto> getInstructorNotification(Instructor instructor) {
        List<Notification> notifications = instructor.getNotifications();
        List<NotificationDto> notificationDtos = new ArrayList<>();
        for (Notification notification : notifications) {
            notificationDtos.add(new NotificationDto(notification));
        }
        return notificationDtos;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void purgeExpiredLiveActivityAlerts() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        long deleted = notificationRepository.deleteByTimeBeforeAndMessageContaining(cutoff, LIVE_ACTIVITY_MARKER);
        if (deleted > 0) {
            log.info("Purged {} expired live activity alerts older than {}", deleted, cutoff);
        }
    }

    private String normalizeMessage(String raw) {
        if (raw == null) {
            return "";
        }
        String compact = raw.replaceAll("\\s+", " ").trim();
        if (compact.length() <= MAX_NOTIFICATION_MESSAGE_LENGTH) {
            return compact;
        }
        return compact.substring(0, MAX_NOTIFICATION_MESSAGE_LENGTH - 3) + "...";
    }
}
