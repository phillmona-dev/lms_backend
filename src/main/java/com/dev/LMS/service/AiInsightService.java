package com.dev.LMS.service;

import com.dev.LMS.dto.AiInsightDto;
import com.dev.LMS.dto.StudentProgressSummaryDto;
import com.dev.LMS.dto.StudentRiskInsightDto;
import com.dev.LMS.model.Role;
import com.dev.LMS.model.User;
import com.dev.LMS.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AiInsightService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final ProgressMonitoringService progressMonitoringService;

    public AiInsightService(UserService userService,
                            UserRepository userRepository,
                            ProgressMonitoringService progressMonitoringService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.progressMonitoringService = progressMonitoringService;
    }

    public List<AiInsightDto> getSchoolInsights(String email) {
        User requester = userService.getUserByEmail(email);
        Role role = requester.getRole() == null ? null : requester.getRole().canonical();
        if (role != Role.SCHOOL_ADMINISTRATOR && role != Role.SYSTEM_ADMINISTRATOR && role != Role.AI_SYSTEM) {
            throw new IllegalStateException("You are not authorized to access AI insights.");
        }

        List<StudentProgressSummaryDto> summaries = userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && user.getRole().canonical() == Role.STUDENT)
                .map(user -> progressMonitoringService.getStudentProgress(user.getPublicId(), user.getPublicId()))
                .toList();

        long highRiskStudents = summaries.stream().filter(this::isHighRisk).count();
        long lowAttendanceStudents = summaries.stream().filter(summary -> summary.getAttendanceRate() < 60.0).count();
        long harmfulContentCases = summaries.stream().filter(summary -> summary.getHarmfulContentReportCount() > 0).count();

        List<AiInsightDto> insights = new ArrayList<>();
        insights.add(new AiInsightDto(
                "DROP_OUT_RISK",
                highRiskStudents > 0 ? "HIGH" : "LOW",
                highRiskStudents + " students show combined low attendance, weak assignment completion, or repeated behavior concerns.",
                "Prioritize case management for the highest-risk students and involve parents plus class teachers."
        ));
        insights.add(new AiInsightDto(
                "ATTENDANCE_PATTERN",
                lowAttendanceStudents > 0 ? "MEDIUM" : "LOW",
                lowAttendanceStudents + " students have attendance below 60%.",
                "Review lesson participation barriers and follow up with guardians for persistent absenteeism."
        ));
        insights.add(new AiInsightDto(
                "HARMFUL_CONTENT_ALERT",
                harmfulContentCases > 0 ? "HIGH" : "LOW",
                harmfulContentCases + " students currently have at least one harmful-content-related behavior report.",
                "Escalate harmful content cases for rapid safeguarding review and targeted support."
        ));
        return insights;
    }

    public List<StudentRiskInsightDto> getStudentRiskInsights(String email) {
        User requester = userService.getUserByEmail(email);
        Role role = requester.getRole() == null ? null : requester.getRole().canonical();
        if (role != Role.SCHOOL_ADMINISTRATOR && role != Role.SYSTEM_ADMINISTRATOR && role != Role.AI_SYSTEM) {
            throw new IllegalStateException("You are not authorized to access AI risk insights.");
        }

        return userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && user.getRole().canonical() == Role.STUDENT)
                .map(user -> progressMonitoringService.getStudentProgress(user.getPublicId(), user.getPublicId()))
                .map(this::toRiskInsight)
                .sorted(Comparator.comparingInt((StudentRiskInsightDto insight) -> riskRank(insight.getRiskLevel())).reversed()
                        .thenComparing(StudentRiskInsightDto::getStudentName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private StudentRiskInsightDto toRiskInsight(StudentProgressSummaryDto summary) {
        String riskLevel = "LOW";
        List<String> reasons = new ArrayList<>();

        if (summary.getAttendanceRate() < 50.0) {
            riskLevel = "HIGH";
            reasons.add("attendance below 50%");
        } else if (summary.getAttendanceRate() < 70.0) {
            riskLevel = "MEDIUM";
            reasons.add("attendance below 70%");
        }

        if (summary.getAssignmentCompletionRate() < 50.0) {
            riskLevel = "HIGH";
            reasons.add("assignment completion below 50%");
        } else if (summary.getAssignmentCompletionRate() < 70.0 && !"HIGH".equals(riskLevel)) {
            riskLevel = "MEDIUM";
            reasons.add("assignment completion below 70%");
        }

        if (summary.getHarmfulContentReportCount() > 0) {
            riskLevel = "HIGH";
            reasons.add("harmful content report present");
        } else if (summary.getBehaviorReportCount() >= 3 && !"HIGH".equals(riskLevel)) {
            riskLevel = "MEDIUM";
            reasons.add("multiple behavior reports");
        }

        if (reasons.isEmpty()) {
            reasons.add("no major risk signals detected");
        }

        return new StudentRiskInsightDto(
                summary.getStudentId(),
                summary.getStudentName(),
                riskLevel,
                String.join("; ", reasons),
                summary.getAttendanceRate(),
                summary.getAssignmentCompletionRate(),
                summary.getBehaviorReportCount(),
                summary.getHarmfulContentReportCount()
        );
    }

    private boolean isHighRisk(StudentProgressSummaryDto summary) {
        return "HIGH".equals(toRiskInsight(summary).getRiskLevel());
    }

    private int riskRank(String riskLevel) {
        return switch (riskLevel) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            default -> 1;
        };
    }
}
