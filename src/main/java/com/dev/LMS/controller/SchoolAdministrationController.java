package com.dev.LMS.controller;

import com.dev.LMS.dto.AnnouncementDto;
import com.dev.LMS.dto.AnnouncementRequestDto;
import com.dev.LMS.dto.SchoolAdminDashboardDto;
import com.dev.LMS.dto.StudentProgressSummaryDto;
import com.dev.LMS.service.SchoolAdministrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/school-admin")
@Tag(name = "School Administration", description = "Endpoints for school-level reports, dashboard monitoring, and announcements.")
public class SchoolAdministrationController {
    private final SchoolAdministrationService schoolAdministrationService;

    public SchoolAdministrationController(SchoolAdministrationService schoolAdministrationService) {
        this.schoolAdministrationService = schoolAdministrationService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get school dashboard", description = "Returns school-wide totals and performance indicators for school administrators.")
    public ResponseEntity<?> getDashboard() {
        try {
            SchoolAdminDashboardDto response = schoolAdministrationService.getDashboard(currentEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/reports/students")
    @Operation(summary = "Get school student progress report", description = "Returns school-wide student progress summaries for school administrators.")
    public ResponseEntity<?> getStudentReports() {
        try {
            List<StudentProgressSummaryDto> response = schoolAdministrationService.getStudentProgressReport(currentEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/announcements")
    @Operation(summary = "Publish an announcement", description = "Creates a school announcement and broadcasts notifications to matching audiences.")
    public ResponseEntity<?> publishAnnouncement(@Valid @RequestBody AnnouncementRequestDto requestDto) {
        try {
            AnnouncementDto response = schoolAdministrationService.publishAnnouncement(currentEmail(), requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/announcements")
    @Operation(summary = "List announcements", description = "Returns announcements in reverse chronological order.")
    public ResponseEntity<?> getAnnouncements() {
        try {
            return ResponseEntity.ok(schoolAdministrationService.getAnnouncements(currentEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
