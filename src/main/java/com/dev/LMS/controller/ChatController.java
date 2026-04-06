package com.dev.LMS.controller;

import com.dev.LMS.dto.ChatMessageRequestDto;
import com.dev.LMS.model.ChatContextType;
import com.dev.LMS.model.User;
import com.dev.LMS.service.ChatService;
import com.dev.LMS.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@Tag(name = "Chat", description = "Context-based discussion channels for courses, lessons, quizzes, assignments, and resources.")
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;

    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @GetMapping("/messages")
    @Operation(summary = "List chat messages", description = "Returns recent messages for a context.")
    public ResponseEntity<?> getMessages(
            @RequestParam ChatContextType contextType,
            @RequestParam String contextKey
    ) {
        try {
            User currentUser = getCurrentUser();
            return ResponseEntity.ok(chatService.getMessages(contextType, contextKey, currentUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/messages")
    @Operation(summary = "Send chat message", description = "Adds a new message to a context channel.")
    public ResponseEntity<?> postMessage(
            @RequestParam ChatContextType contextType,
            @RequestParam String contextKey,
            @RequestBody ChatMessageRequestDto request
    ) {
        try {
            User currentUser = getCurrentUser();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(chatService.postMessage(contextType, contextKey, request.getMessage(), currentUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }
}
