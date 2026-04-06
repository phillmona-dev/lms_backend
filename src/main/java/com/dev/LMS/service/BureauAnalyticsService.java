package com.dev.LMS.service;

import com.dev.LMS.dto.BureauDashboardDto;
import com.dev.LMS.dto.BureauSchoolRankingDto;
import com.dev.LMS.dto.NationalExamAnalysisDto;
import com.dev.LMS.dto.NationalExamSchoolResultDto;
import com.dev.LMS.dto.SchoolComplianceDto;
import com.dev.LMS.dto.SchoolCurriculumDto;
import com.dev.LMS.dto.SchoolPerformanceDto;
import com.dev.LMS.dto.StudentProgressSummaryDto;
import com.dev.LMS.model.Course;
import com.dev.LMS.model.Instructor;
import com.dev.LMS.model.Role;
import com.dev.LMS.model.School;
import com.dev.LMS.model.Student;
import com.dev.LMS.model.User;
import com.dev.LMS.repository.CourseRepository;
import com.dev.LMS.repository.SchoolRepository;
import com.dev.LMS.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BureauAnalyticsService {
    private static final double TARGET_COURSE_COUNT = 12.0;
    private static final Map<String, List<String>> CORE_SUBJECT_KEYWORDS = Map.of(
            "Mathematics", List.of("math", "mathematics", "algebra", "geometry", "calculus"),
            "English", List.of("english", "language", "literature", "writing", "grammar"),
            "Physics", List.of("physics", "mechanics", "electricity"),
            "Chemistry", List.of("chemistry", "chemical", "organic"),
            "Biology", List.of("biology", "life science", "genetics", "ecology"),
            "History", List.of("history", "historical", "civilization"),
            "Geography", List.of("geography", "map", "climate"),
            "ICT", List.of("ict", "computer", "coding", "programming", "technology")
    );

    private final UserService userService;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final CourseRepository courseRepository;
    private final ProgressMonitoringService progressMonitoringService;

    public BureauAnalyticsService(UserService userService,
                                  UserRepository userRepository,
                                  SchoolRepository schoolRepository,
                                  CourseRepository courseRepository,
                                  ProgressMonitoringService progressMonitoringService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.courseRepository = courseRepository;
        this.progressMonitoringService = progressMonitoringService;
    }

    public BureauDashboardDto getDashboard(String email) {
        ensureBureauUser(email);
        List<SchoolPerformanceDto> schools = getSchoolPerformance(email);
        return new BureauDashboardDto(
                schools.size(),
                schools.stream().mapToLong(SchoolPerformanceDto::getStudentCount).sum(),
                schools.stream().mapToLong(SchoolPerformanceDto::getTeacherCount).sum(),
                schools.stream().mapToLong(SchoolPerformanceDto::getCourseCount).sum(),
                average(schools.stream().mapToDouble(SchoolPerformanceDto::getAverageAttendanceRate).toArray()),
                average(schools.stream().mapToDouble(SchoolPerformanceDto::getAverageAssignmentCompletionRate).toArray()),
                average(schools.stream().mapToDouble(SchoolPerformanceDto::getAverageOverallProgressRate).toArray()),
                schools.stream().mapToLong(SchoolPerformanceDto::getBehaviorReports).sum(),
                schools.stream().mapToLong(SchoolPerformanceDto::getHarmfulContentReports).sum(),
                schools.stream().mapToLong(SchoolPerformanceDto::getAtRiskStudents).sum()
        );
    }

    public List<SchoolPerformanceDto> getSchoolPerformance(String email) {
        ensureBureauUser(email);
        return buildSchoolSnapshots().stream()
                .map(this::buildSchoolPerformance)
                .sorted(Comparator.comparing(SchoolPerformanceDto::getAverageOverallProgressRate).reversed())
                .toList();
    }

    public List<SchoolPerformanceDto> getHighRiskSchools(String email) {
        ensureBureauUser(email);
        return buildSchoolSnapshots().stream()
                .map(this::buildSchoolPerformance)
                .filter(school -> school.getAtRiskStudents() > 0
                        || school.getHarmfulContentReports() > 0
                        || school.getAverageAttendanceRate() < 60.0)
                .sorted(Comparator.comparingLong(SchoolPerformanceDto::getAtRiskStudents).reversed()
                        .thenComparingLong(SchoolPerformanceDto::getHarmfulContentReports).reversed())
                .toList();
    }

    public List<SchoolCurriculumDto> getCurriculumCoverage(String email) {
        ensureBureauUser(email);
        return buildSchoolSnapshots().stream()
                .map(this::buildCurriculumCoverage)
                .sorted(Comparator.comparing(SchoolCurriculumDto::getCurriculumCoverageRate).reversed())
                .toList();
    }

    public List<BureauSchoolRankingDto> getSchoolRanking(String email) {
        ensureBureauUser(email);
        List<SchoolSnapshot> snapshots = buildSchoolSnapshots();

        Map<Long, SchoolComplianceDto> complianceBySchool = getSchoolCompliance(email).stream()
                .collect(Collectors.toMap(SchoolComplianceDto::getSchoolId, dto -> dto));

        Map<Long, NationalExamSchoolResultDto> examBySchool = getNationalExamAnalysis(email).getSchoolResults().stream()
                .collect(Collectors.toMap(NationalExamSchoolResultDto::getSchoolId, dto -> dto));

        List<SchoolPerformanceDto> performanceDtos = snapshots.stream()
                .map(this::buildSchoolPerformance)
                .toList();

        List<BureauSchoolRankingDto> unsorted = new ArrayList<>();
        for (SchoolPerformanceDto performance : performanceDtos) {
            SchoolComplianceDto compliance = complianceBySchool.get(performance.getSchoolId());
            NationalExamSchoolResultDto exam = examBySchool.get(performance.getSchoolId());

            double complianceRate = compliance == null ? 0.0 : compliance.getOverallComplianceRate();
            double examReadiness = exam == null ? 0.0 : exam.getAverageScore();
            double riskPenalty = Math.min(100.0, (performance.getAtRiskStudents() * 3.0) + (performance.getHarmfulContentReports() * 5.0));
            double riskResilience = 100.0 - riskPenalty;

            double score = round(
                    (performance.getAverageOverallProgressRate() * 0.35)
                            + (performance.getAverageAttendanceRate() * 0.20)
                            + (examReadiness * 0.20)
                            + (complianceRate * 0.15)
                            + (riskResilience * 0.10)
            );

            unsorted.add(new BureauSchoolRankingDto(
                    0,
                    performance.getSchoolId(),
                    performance.getSchoolName(),
                    performance.getRegion(),
                    score,
                    performance.getAverageAttendanceRate(),
                    performance.getAverageOverallProgressRate(),
                    examReadiness,
                    complianceRate,
                    performance.getAtRiskStudents()
            ));
        }

        List<BureauSchoolRankingDto> sorted = unsorted.stream()
                .sorted(Comparator.comparing(BureauSchoolRankingDto::getPerformanceScore).reversed())
                .toList();

        List<BureauSchoolRankingDto> ranked = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            BureauSchoolRankingDto row = sorted.get(i);
            ranked.add(new BureauSchoolRankingDto(
                    i + 1,
                    row.getSchoolId(),
                    row.getSchoolName(),
                    row.getRegion(),
                    row.getPerformanceScore(),
                    row.getAverageAttendanceRate(),
                    row.getAverageOverallProgressRate(),
                    row.getExamReadinessRate(),
                    row.getComplianceRate(),
                    row.getAtRiskStudents()
            ));
        }
        return ranked;
    }

    public NationalExamAnalysisDto getNationalExamAnalysis(String email) {
        ensureBureauUser(email);
        List<NationalExamSchoolResultDto> schoolResults = buildSchoolSnapshots().stream()
                .map(this::buildExamResult)
                .sorted(Comparator.comparing(NationalExamSchoolResultDto::getAverageScore).reversed())
                .toList();

        double nationalAverageScore = average(schoolResults.stream().mapToDouble(NationalExamSchoolResultDto::getAverageScore).toArray());
        double nationalPassRate = average(schoolResults.stream().mapToDouble(NationalExamSchoolResultDto::getPassRate).toArray());
        long totalCandidates = schoolResults.stream().mapToLong(NationalExamSchoolResultDto::getCandidates).sum();
        long highPerforming = schoolResults.stream().filter(result -> result.getAverageScore() >= 70 && result.getPassRate() >= 75).count();
        long supportNeeded = schoolResults.stream().filter(result -> result.getAverageScore() < 55 || result.getPassRate() < 55).count();

        String insight = buildExamInsight(nationalAverageScore, nationalPassRate, highPerforming, supportNeeded);

        return new NationalExamAnalysisDto(
                nationalAverageScore,
                nationalPassRate,
                totalCandidates,
                highPerforming,
                supportNeeded,
                insight,
                schoolResults.stream().limit(5).toList(),
                schoolResults
        );
    }

    public List<SchoolComplianceDto> getSchoolCompliance(String email) {
        ensureBureauUser(email);

        return buildSchoolSnapshots().stream()
                .map(snapshot -> {
                    SchoolPerformanceDto performance = buildSchoolPerformance(snapshot);
                    SchoolCurriculumDto curriculum = buildCurriculumCoverage(snapshot);

                    double curriculumCompliance = round((curriculum.getCurriculumCoverageRate() * 0.7)
                            + (curriculum.getCoreSubjectCoverageRate() * 0.3));
                    double attendanceCompliance = performance.getAverageAttendanceRate();
                    double safetyCompliance = round(100.0 - Math.min(100.0,
                            (performance.getHarmfulContentReports() * 6.0)
                                    + (performance.getAtRiskStudents() * 2.0)));
                    double reportingCompliance = round(Math.min(100.0,
                            60.0
                                    + (performance.getCourseCount() * 4.0)
                                    + (performance.getTeacherCount() * 2.0)
                                    + (performance.getStudentCount() > 0 ? 10.0 : 0.0)));

                    double overall = round((curriculumCompliance * 0.30)
                            + (attendanceCompliance * 0.30)
                            + (safetyCompliance * 0.25)
                            + (reportingCompliance * 0.15));

                    String status = overall >= 80 ? "COMPLIANT" : overall >= 65 ? "PARTIAL" : "NON_COMPLIANT";
                    String notes = buildComplianceNotes(curriculumCompliance, attendanceCompliance, safetyCompliance, reportingCompliance);

                    return new SchoolComplianceDto(
                            snapshot.school().getId(),
                            snapshot.school().getName(),
                            snapshot.school().getRegion(),
                            curriculumCompliance,
                            attendanceCompliance,
                            safetyCompliance,
                            reportingCompliance,
                            overall,
                            status,
                            notes
                    );
                })
                .sorted(Comparator.comparing(SchoolComplianceDto::getOverallComplianceRate).reversed())
                .toList();
    }

    private SchoolPerformanceDto buildSchoolPerformance(SchoolSnapshot snapshot) {
        return new SchoolPerformanceDto(
                snapshot.school().getId(),
                snapshot.school().getName(),
                snapshot.school().getRegion(),
                snapshot.students().size(),
                snapshot.teachers().size(),
                snapshot.courses().size(),
                average(snapshot.studentSummaries().stream().mapToDouble(StudentProgressSummaryDto::getAttendanceRate).toArray()),
                average(snapshot.studentSummaries().stream().mapToDouble(StudentProgressSummaryDto::getAssignmentCompletionRate).toArray()),
                average(snapshot.studentSummaries().stream().mapToDouble(StudentProgressSummaryDto::getOverallProgressRate).toArray()),
                snapshot.studentSummaries().stream().mapToLong(StudentProgressSummaryDto::getBehaviorReportCount).sum(),
                snapshot.studentSummaries().stream().mapToLong(StudentProgressSummaryDto::getHarmfulContentReportCount).sum(),
                snapshot.studentSummaries().stream().filter(this::isAtRisk).count()
        );
    }

    private SchoolCurriculumDto buildCurriculumCoverage(SchoolSnapshot snapshot) {
        long totalCourses = snapshot.courses().size();
        long totalLessons = snapshot.courses().stream().mapToLong(course -> course.getLessons() == null ? 0 : course.getLessons().size()).sum();
        long totalAssignments = snapshot.courses().stream().mapToLong(course -> course.getAssignments() == null ? 0 : course.getAssignments().size()).sum();
        double averageLessonsPerCourse = totalCourses == 0 ? 0.0 : round(totalLessons / (double) totalCourses);

        double curriculumCoverageRate = round(Math.min(100.0, (totalCourses / TARGET_COURSE_COUNT) * 100.0));

        Set<String> coveredSubjects = detectCoveredSubjects(snapshot.courses());
        double coreSubjectCoverageRate = CORE_SUBJECT_KEYWORDS.isEmpty()
                ? 0.0
                : round((coveredSubjects.size() * 100.0) / CORE_SUBJECT_KEYWORDS.size());

        List<String> missingSubjects = CORE_SUBJECT_KEYWORDS.keySet().stream()
                .filter(subject -> !coveredSubjects.contains(subject))
                .sorted(String::compareToIgnoreCase)
                .toList();

        return new SchoolCurriculumDto(
                snapshot.school().getId(),
                snapshot.school().getName(),
                snapshot.school().getRegion(),
                totalCourses,
                totalLessons,
                totalAssignments,
                averageLessonsPerCourse,
                curriculumCoverageRate,
                coreSubjectCoverageRate,
                missingSubjects
        );
    }

    private NationalExamSchoolResultDto buildExamResult(SchoolSnapshot snapshot) {
        List<StudentProgressSummaryDto> summaries = snapshot.studentSummaries();
        if (summaries.isEmpty()) {
            return new NationalExamSchoolResultDto(
                    snapshot.school().getId(),
                    snapshot.school().getName(),
                    snapshot.school().getRegion(),
                    0,
                    0.0,
                    0.0,
                    0.0,
                    "LOW"
            );
        }

        List<Double> scores = summaries.stream().map(this::toExamScore).toList();
        double avgScore = average(scores.stream().mapToDouble(Double::doubleValue).toArray());
        double passRate = round(scores.stream().filter(score -> score >= 50.0).count() * 100.0 / scores.size());
        double distinctionRate = round(scores.stream().filter(score -> score >= 75.0).count() * 100.0 / scores.size());

        String readiness = avgScore >= 70 && passRate >= 75
                ? "HIGH"
                : (avgScore >= 55 && passRate >= 60 ? "MEDIUM" : "LOW");

        return new NationalExamSchoolResultDto(
                snapshot.school().getId(),
                snapshot.school().getName(),
                snapshot.school().getRegion(),
                scores.size(),
                avgScore,
                passRate,
                distinctionRate,
                readiness
        );
    }

    private Set<String> detectCoveredSubjects(List<Course> courses) {
        Set<String> covered = CORE_SUBJECT_KEYWORDS.keySet().stream()
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        covered.clear();

        for (Course course : courses) {
            String title = normalize(course.getName());
            String description = normalize(course.getDescription());
            String corpus = title + " " + description;

            for (Map.Entry<String, List<String>> entry : CORE_SUBJECT_KEYWORDS.entrySet()) {
                boolean matched = entry.getValue().stream().anyMatch(corpus::contains);
                if (matched) {
                    covered.add(entry.getKey());
                }
            }
        }
        return covered;
    }

    private double toExamScore(StudentProgressSummaryDto summary) {
        double attendance = summary.getAttendanceRate();
        double assignment = summary.getAssignmentCompletionRate();
        double overall = summary.getOverallProgressRate();
        double quiz = summary.getAverageQuizGrade() == null ? overall : summary.getAverageQuizGrade();

        double score = (overall * 0.35) + (attendance * 0.25) + (assignment * 0.25) + (quiz * 0.15);

        score -= Math.min(10.0, summary.getHarmfulContentReportCount() * 2.0);
        score -= Math.min(6.0, summary.getBehaviorReportCount() * 0.8);

        return round(Math.max(0.0, Math.min(100.0, score)));
    }

    private List<SchoolSnapshot> buildSchoolSnapshots() {
        List<School> schools = schoolRepository.findAll();
        List<User> users = userRepository.findAll();
        List<Course> courses = courseRepository.findAll();

        Map<Long, List<User>> usersBySchool = users.stream()
                .filter(user -> user.getSchool() != null && user.getSchool().getId() != null)
                .collect(Collectors.groupingBy(user -> user.getSchool().getId()));

        Map<Long, List<Course>> coursesBySchool = courses.stream()
                .filter(course -> course.getSchool() != null && course.getSchool().getId() != null)
                .collect(Collectors.groupingBy(course -> course.getSchool().getId()));

        List<SchoolSnapshot> snapshots = new ArrayList<>();
        for (School school : schools) {
            List<User> schoolUsers = usersBySchool.getOrDefault(school.getId(), List.of());
            List<Course> schoolCourses = coursesBySchool.getOrDefault(school.getId(), List.of());

            List<Student> students = schoolUsers.stream()
                    .filter(user -> user instanceof Student)
                    .map(user -> (Student) user)
                    .toList();

            List<Instructor> teachers = schoolUsers.stream()
                    .filter(user -> user instanceof Instructor)
                    .map(user -> (Instructor) user)
                    .toList();

            List<StudentProgressSummaryDto> studentSummaries = students.stream()
                    .map(student -> progressMonitoringService.getStudentProgress(student.getPublicId(), student.getPublicId()))
                    .toList();

            snapshots.add(new SchoolSnapshot(school, students, teachers, schoolCourses, studentSummaries));
        }

        return snapshots;
    }

    private String buildExamInsight(double nationalAverageScore,
                                    double nationalPassRate,
                                    long highPerforming,
                                    long supportNeeded) {
        if (supportNeeded > highPerforming) {
            return "National exam readiness is uneven. Priority intervention is recommended for low-performing schools with targeted remedial plans and teacher support.";
        }
        if (nationalAverageScore >= 70 && nationalPassRate >= 75) {
            return "National exam performance is strong. Scale best practices from top schools and maintain continuous quality audits.";
        }
        return "National exam outcomes are moderate. Strengthen curriculum pacing, revision cycles, and attendance follow-up to improve pass rates.";
    }

    private String buildComplianceNotes(double curriculum,
                                        double attendance,
                                        double safety,
                                        double reporting) {
        Map<String, Double> dimensions = new LinkedHashMap<>();
        dimensions.put("curriculum", curriculum);
        dimensions.put("attendance", attendance);
        dimensions.put("safety", safety);
        dimensions.put("reporting", reporting);

        String weakest = dimensions.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("overall");

        return "Primary compliance gap detected in " + weakest + ".";
    }

    private String normalize(String value) {
        return String.valueOf(value == null ? "" : value).toLowerCase();
    }

    private void ensureBureauUser(String email) {
        User user = userService.getUserByEmail(email);
        Role role = user.getRole() == null ? null : user.getRole().canonical();
        if (role != Role.BUREAU_OF_EDUCATION && role != Role.SYSTEM_ADMINISTRATOR) {
            throw new IllegalStateException("Only bureau or system administrators can access regional analytics.");
        }
    }

    private double average(double[] values) {
        if (values.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return round(sum / values.length);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private boolean isAtRisk(StudentProgressSummaryDto summary) {
        return summary.getAttendanceRate() < 50.0
                || summary.getAssignmentCompletionRate() < 50.0
                || summary.getHarmfulContentReportCount() > 0
                || summary.getBehaviorReportCount() >= 3;
    }

    private record SchoolSnapshot(School school,
                                  List<Student> students,
                                  List<Instructor> teachers,
                                  List<Course> courses,
                                  List<StudentProgressSummaryDto> studentSummaries) {
    }
}
