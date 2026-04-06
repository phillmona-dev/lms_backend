package com.dev.LMS.controller;

import com.dev.LMS.service.AiInsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai-insights")
@Tag(name = "AI Insights", description = "Heuristic AI-style insights and risk indicators derived from LMS data.")
public class AiInsightController {
    private final AiInsightService aiInsightService;

    public AiInsightController(AiInsightService aiInsightService) {
        this.aiInsightService = aiInsightService;
    }

    @GetMapping("/school")
    @Operation(summary = "Get school AI insights", description = "Returns AI-style school insights derived from performance, attendance, and behavior signals.")
    public ResponseEntity<?> getSchoolInsights() {
        try {
            return ResponseEntity.ok(aiInsightService.getSchoolInsights(currentEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/students/risk")
    @Operation(summary = "Get student risk insights", description = "Returns student-level risk classifications for dropout, low performance, and behavioral concern monitoring.")
    public ResponseEntity<?> getStudentRiskInsights() {
        try {
            return ResponseEntity.ok(aiInsightService.getStudentRiskInsights(currentEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
