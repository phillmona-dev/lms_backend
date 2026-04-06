package com.dev.LMS.repository;

import com.dev.LMS.model.ChatContextType;
import com.dev.LMS.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop200ByContextTypeAndContextKeyOrderByCreatedAtDesc(
            ChatContextType contextType,
            String contextKey
    );
}
