package com.dev.LMS.repository;

import com.dev.LMS.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long deleteByTimeBeforeAndMessageContaining(LocalDateTime cutoff, String marker);
}
