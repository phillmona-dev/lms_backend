package com.dev.LMS.dto;

import com.dev.LMS.model.ChatMessage;
import com.dev.LMS.model.User;

import java.time.LocalDateTime;

public class ChatMessageDto {
    private Long id;
    private String contextType;
    private String contextKey;
    private String message;
    private String senderName;
    private String senderRole;
    private String senderEmail;
    private boolean mine;
    private LocalDateTime createdAt;

    public static ChatMessageDto from(ChatMessage entity, User currentUser) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(entity.getId());
        dto.setContextType(entity.getContextType() != null ? entity.getContextType().name() : null);
        dto.setContextKey(entity.getContextKey());
        dto.setMessage(entity.getMessage());
        dto.setSenderName(entity.getSender() != null ? entity.getSender().getName() : "Unknown user");
        dto.setSenderRole(
                entity.getSender() != null && entity.getSender().getRole() != null
                        ? entity.getSender().getRole().name()
                        : null
        );
        dto.setSenderEmail(entity.getSender() != null ? entity.getSender().getEmail() : null);
        dto.setMine(
                currentUser != null
                        && entity.getSender() != null
                        && entity.getSender().getId() == currentUser.getId()
        );
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContextType() {
        return contextType;
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    public String getContextKey() {
        return contextKey;
    }

    public void setContextKey(String contextKey) {
        this.contextKey = contextKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
