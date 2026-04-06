package com.dev.LMS.service;

import com.dev.LMS.dto.ChatMessageDto;
import com.dev.LMS.model.ChatContextType;
import com.dev.LMS.model.ChatMessage;
import com.dev.LMS.model.User;
import com.dev.LMS.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;

    public ChatService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(ChatContextType contextType, String contextKey, User user) {
        String normalizedKey = normalizeContextKey(contextKey);
        return chatMessageRepository
                .findTop200ByContextTypeAndContextKeyOrderByCreatedAtDesc(contextType, normalizedKey)
                .stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(message -> ChatMessageDto.from(message, user))
                .toList();
    }

    @Transactional
    public ChatMessageDto postMessage(ChatContextType contextType, String contextKey, String message, User sender) {
        String normalizedMessage = normalizeMessage(message);
        if (normalizedMessage.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }

        ChatMessage entity = new ChatMessage();
        entity.setContextType(contextType);
        entity.setContextKey(normalizeContextKey(contextKey));
        entity.setMessage(normalizedMessage);
        entity.setSender(sender);

        ChatMessage saved = chatMessageRepository.save(entity);
        return ChatMessageDto.from(saved, sender);
    }

    private String normalizeContextKey(String contextKey) {
        if (contextKey == null) {
            throw new IllegalArgumentException("Context key is required.");
        }
        String value = contextKey.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Context key is required.");
        }
        if (value.length() > 260) {
            throw new IllegalArgumentException("Context key is too long.");
        }
        return value;
    }

    private String normalizeMessage(String message) {
        if (message == null) {
            return "";
        }
        String value = message.trim();
        if (value.length() > 3000) {
            throw new IllegalArgumentException("Message is too long. Maximum is 3000 characters.");
        }
        return value;
    }
}
