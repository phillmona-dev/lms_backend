package com.dev.LMS.dto;

import com.dev.LMS.model.LiveSession;

import java.time.LocalDateTime;

public class LiveSessionDto {
    private Long id;
    private String topic;
    private String roomName;
    private String joinUrl;
    private boolean active;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String startedByName;

    public LiveSessionDto() {
    }

    public LiveSessionDto(LiveSession session) {
        this.id = session.getId();
        this.topic = session.getTopic();
        this.roomName = session.getRoomName();
        this.joinUrl = session.getJoinUrl();
        this.active = session.isActive();
        this.startedAt = session.getStartedAt();
        this.endedAt = session.getEndedAt();
        this.startedByName = session.getStartedBy() != null ? session.getStartedBy().getName() : null;
    }

    public Long getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getJoinUrl() {
        return joinUrl;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public String getStartedByName() {
        return startedByName;
    }
}
