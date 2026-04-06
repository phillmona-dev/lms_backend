package com.dev.LMS.controller;

import com.dev.LMS.dto.LiveSessionActivityRequest;
import com.dev.LMS.dto.LiveSessionDto;
import com.dev.LMS.service.LiveSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/live")
@Tag(name = "Live Sessions", description = "Live classroom sessions for teacher-led online classes.")
public class LiveSessionController {
    private static final Logger log = LoggerFactory.getLogger(LiveSessionController.class);

    private final LiveSessionService liveSessionService;

    public LiveSessionController(LiveSessionService liveSessionService) {
        this.liveSessionService = liveSessionService;
    }

    @PostMapping("/course/{courseName}/sessions/start")
    @Operation(summary = "Start a live session", description = "Starts a live classroom session for a course. Teacher-only.")
    public ResponseEntity<LiveSessionDto> startSession(@PathVariable String courseName,
                                                       @RequestBody(required = false) Map<String, String> payload) {
        String topic = payload == null ? null : payload.get("topic");
        String requesterEmail = currentEmail();
        return ResponseEntity.ok(liveSessionService.startSession(courseName, requesterEmail, topic));
    }

    @PostMapping("/course/{courseName}/sessions/{sessionId}/end")
    @Operation(summary = "End a live session", description = "Ends an active live session for a course. Teacher-only.")
    public ResponseEntity<LiveSessionDto> endSession(@PathVariable String courseName,
                                                     @PathVariable Long sessionId) {
        String requesterEmail = currentEmail();
        return ResponseEntity.ok(liveSessionService.endSession(courseName, sessionId, requesterEmail));
    }

    @GetMapping("/course/{courseName}/sessions/active")
    @Operation(summary = "Get active session", description = "Returns the currently active live session for the course.")
    public ResponseEntity<LiveSessionDto> getActiveSession(@PathVariable String courseName) {
        String requesterEmail = currentEmail();
        return ResponseEntity.ok(liveSessionService.getActiveSession(courseName, requesterEmail));
    }

    @GetMapping("/course/{courseName}/sessions")
    @Operation(summary = "List sessions", description = "Returns live session history for the course.")
    public ResponseEntity<List<LiveSessionDto>> getSessions(@PathVariable String courseName) {
        String requesterEmail = currentEmail();
        return ResponseEntity.ok(liveSessionService.getSessions(courseName, requesterEmail));
    }

    @PostMapping("/course/{courseName}/sessions/{sessionId}/activity")
    @Operation(summary = "Report live session activity", description = "Student client activity event reporting during a live classroom session.")
    public ResponseEntity<Void> reportActivity(@PathVariable String courseName,
                                               @PathVariable Long sessionId,
                                               @RequestBody LiveSessionActivityRequest request,
                                               HttpServletRequest servletRequest) {
        String requesterEmail = currentEmail();
        log.info("Received live activity event course='{}' sessionId={} student='{}' eventType='{}'",
                courseName, sessionId, requesterEmail, request == null ? "null" : request.getEventType());
        liveSessionService.reportStudentActivity(
                courseName,
                sessionId,
                requesterEmail,
                request,
                extractClientIp(servletRequest)
        );
        return ResponseEntity.accepted().build();
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
