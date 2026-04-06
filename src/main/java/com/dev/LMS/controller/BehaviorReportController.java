package com.dev.LMS.controller;

import com.dev.LMS.dto.BehaviorReportDto;
import com.dev.LMS.dto.BehaviorReportRequestDto;
import com.dev.LMS.service.BehaviorReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/behavior-reports")
@Tag(name = "Behavior Reports", description = "Endpoints for teacher behavior reporting and student behavior monitoring.")
public class BehaviorReportController {
    private final BehaviorReportService behaviorReportService;

    public BehaviorReportController(BehaviorReportService behaviorReportService) {
        this.behaviorReportService = behaviorReportService;
    }

    @PostMapping("/courses/{courseName}/students/{studentId}")
    @Operation(summary = "Create a behavior report", description = "Allows the teacher of a course to create a behavior report for an enrolled student.")
    public ResponseEntity<?> createBehaviorReport(@PathVariable String courseName,
                                                  @PathVariable UUID studentId,
                                                  @Valid @RequestBody BehaviorReportRequestDto requestDto) {
        try {
            BehaviorReportDto response = behaviorReportService.createReport(currentEmail(), courseName, studentId, requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me/reported")
    @Operation(summary = "Get my reported behavior issues", description = "Returns all behavior reports created by the authenticated teacher.")
    public ResponseEntity<?> getMyReportedIssues() {
        try {
            List<BehaviorReportDto> response = behaviorReportService.getTeacherReports(currentEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/students/{studentId}")
    @Operation(summary = "Get student behavior reports", description = "Returns behavior reports for a student to authorized viewers such as linked parents, teachers, the student, and administrators.")
    public ResponseEntity<?> getStudentBehaviorReports(@PathVariable UUID studentId) {
        try {
            List<BehaviorReportDto> response = behaviorReportService.getStudentReports(currentEmail(), studentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
