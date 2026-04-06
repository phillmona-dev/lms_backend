package com.dev.LMS.controller;

import com.dev.LMS.dto.ParentStudentLinkDto;
import com.dev.LMS.dto.ParentStudentLinkRequestDto;
import com.dev.LMS.dto.StudentProgressSummaryDto;
import com.dev.LMS.model.User;
import com.dev.LMS.service.ProgressMonitoringService;
import com.dev.LMS.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/progress")
@Tag(name = "Progress Monitoring", description = "Endpoints for student progress tracking, parent-student links, and teacher monitoring views.")
public class ProgressMonitoringController {
    private final ProgressMonitoringService progressMonitoringService;
    private final UserService userService;

    public ProgressMonitoringController(ProgressMonitoringService progressMonitoringService, UserService userService) {
        this.progressMonitoringService = progressMonitoringService;
        this.userService = userService;
    }

    @PostMapping("/parent-links")
    @Operation(summary = "Link a parent to a student", description = "Creates or reactivates a parent-student relationship so the parent can monitor the student's progress.")
    public ResponseEntity<?> linkParentToStudent(@Valid @RequestBody ParentStudentLinkRequestDto requestDto) {
        try {
            User requester = getAuthenticatedUser();
            ensureAdminUser(requester);
            ParentStudentLinkDto response = progressMonitoringService.linkParentToStudent(requestDto.getParentId(), requestDto.getStudentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/parent-links/{parentId}/{studentId}")
    @Operation(summary = "Unlink a parent from a student", description = "Deactivates an existing parent-student monitoring relationship.")
    public ResponseEntity<?> unlinkParentFromStudent(@PathVariable UUID parentId, @PathVariable UUID studentId) {
        try {
            User requester = getAuthenticatedUser();
            ensureAdminUser(requester);
            progressMonitoringService.unlinkParentFromStudent(parentId, studentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get my student progress", description = "Returns the authenticated student's learning progress summary.")
    public ResponseEntity<?> getMyProgress() {
        try {
            return ResponseEntity.ok(progressMonitoringService.getMyProgress(getCurrentEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/teacher/students")
    @Operation(summary = "Get teacher student progress", description = "Returns progress summaries for students enrolled in the authenticated teacher's courses.")
    public ResponseEntity<?> getTeacherStudentProgress() {
        try {
            List<StudentProgressSummaryDto> response = progressMonitoringService.getTeacherStudentProgress(getCurrentEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/students/{studentId}")
    @Operation(summary = "Get one student progress summary", description = "Returns a student progress summary for an authorized viewer such as the student, linked parent, teacher, or administrator.")
    public ResponseEntity<?> getStudentProgress(@PathVariable UUID studentId) {
        try {
            User requester = getAuthenticatedUser();
            return ResponseEntity.ok(progressMonitoringService.getStudentProgress(requester.getPublicId(), studentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/parent-links/me")
    @Operation(summary = "Get my linked students", description = "Returns active parent-student relationships for the authenticated parent.")
    public ResponseEntity<?> getMyParentLinks() {
        try {
            User requester = getAuthenticatedUser();
            return ResponseEntity.ok(progressMonitoringService.getParentLinks(requester.getPublicId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/parent/students")
    @Operation(summary = "Get my children progress", description = "Returns progress summaries for students linked to the authenticated parent.")
    public ResponseEntity<?> getMyChildrenProgress() {
        try {
            User requester = getAuthenticatedUser();
            return ResponseEntity.ok(progressMonitoringService.getLinkedStudentProgress(requester.getPublicId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private User getAuthenticatedUser() {
        return userService.getUserByEmail(getCurrentEmail());
    }

    private String getCurrentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void ensureAdminUser(User requester) {
        if (requester.getRole() == null || !requester.getRole().isAdministrativeRole()) {
            throw new IllegalStateException("Only administrators can manage parent-student links.");
        }
    }
}
