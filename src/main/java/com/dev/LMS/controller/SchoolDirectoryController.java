package com.dev.LMS.controller;

import com.dev.LMS.dto.CourseSchoolAssignmentRequestDto;
import com.dev.LMS.dto.SchoolRequestDto;
import com.dev.LMS.dto.UserSchoolAssignmentRequestDto;
import com.dev.LMS.service.SchoolDirectoryService;
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

@RestController
@RequestMapping("/schools")
@Tag(name = "Schools", description = "Endpoints for school creation and school assignment of users and courses.")
public class SchoolDirectoryController {
    private final SchoolDirectoryService schoolDirectoryService;

    public SchoolDirectoryController(SchoolDirectoryService schoolDirectoryService) {
        this.schoolDirectoryService = schoolDirectoryService;
    }

    @PostMapping
    @Operation(summary = "Create a school", description = "Creates a new school record for cross-school analytics and assignment.")
    public ResponseEntity<?> createSchool(@Valid @RequestBody SchoolRequestDto requestDto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(schoolDirectoryService.createSchool(currentEmail(), requestDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "List schools", description = "Returns all configured schools.")
    public ResponseEntity<?> getSchools() {
        try {
            return ResponseEntity.ok(schoolDirectoryService.getSchools(currentEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/assign-user")
    @Operation(summary = "Assign user to school", description = "Assigns a user to a school.")
    public ResponseEntity<?> assignUserToSchool(@Valid @RequestBody UserSchoolAssignmentRequestDto requestDto) {
        try {
            return ResponseEntity.ok(schoolDirectoryService.assignUserToSchool(currentEmail(), requestDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/assign-course")
    @Operation(summary = "Assign course to school", description = "Assigns a course to a school.")
    public ResponseEntity<?> assignCourseToSchool(@Valid @RequestBody CourseSchoolAssignmentRequestDto requestDto) {
        try {
            return ResponseEntity.ok(schoolDirectoryService.assignCourseToSchool(currentEmail(), requestDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
