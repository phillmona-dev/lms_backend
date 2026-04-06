package com.dev.LMS.controller;

import com.dev.LMS.service.BureauAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bureau")
@Tag(name = "Bureau Analytics", description = "Regional and cross-school analytics for bureau oversight and strategic reporting.")
public class BureauAnalyticsController {
    private final BureauAnalyticsService bureauAnalyticsService;

    public BureauAnalyticsController(BureauAnalyticsService bureauAnalyticsService) {
        this.bureauAnalyticsService = bureauAnalyticsService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get bureau dashboard", description = "Returns aggregate school analytics across the configured schools.")
    public ResponseEntity<?> getDashboard() {
        try {
            return ResponseEntity.ok(bureauAnalyticsService.getDashboard(currentEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/schools/performance")
    @Operation(summary = "Compare school performance", description = "Returns school-by-school performance metrics for bureau comparison.")
    public ResponseEntity<?> getSchoolPerformance() {
        try {
            return ResponseEntity.ok(bureauAnalyticsService.getSchoolPerformance(currentEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/schools/high-risk")
    @Operation(summary = "Identify high-risk schools", description = "Returns schools with low attendance, harmful content concerns, or elevated at-risk learner counts.")
    public ResponseEntity<?> getHighRiskSchools() {
        try {
            return ResponseEntity.ok(bureauAnalyticsService.getHighRiskSchools(currentEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
