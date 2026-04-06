package com.dev.LMS.service;

import com.dev.LMS.model.AppRole;
import com.dev.LMS.model.Privilege;
import com.dev.LMS.model.Role;
import com.dev.LMS.model.User;
import com.dev.LMS.repository.AppRoleRepository;
import com.dev.LMS.repository.PrivilegeRepository;
import com.dev.LMS.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
@Transactional
public class RbacBootstrapService implements CommandLineRunner {
    private final AppRoleRepository appRoleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final UserRepository userRepository;

    public RbacBootstrapService(AppRoleRepository appRoleRepository, PrivilegeRepository privilegeRepository, UserRepository userRepository) {
        this.appRoleRepository = appRoleRepository;
        this.privilegeRepository = privilegeRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        Map<String, String> privilegeDefinitions = new LinkedHashMap<>();
        privilegeDefinitions.put("PROFILE_VIEW", "View own profile");
        privilegeDefinitions.put("PROFILE_UPDATE", "Update own profile");
        privilegeDefinitions.put("COURSE_ENROLL", "Access enrolled courses");
        privilegeDefinitions.put("COURSE_VIEW", "View published course content");
        privilegeDefinitions.put("COURSE_MANAGE", "Create and manage courses");
        privilegeDefinitions.put("LESSON_VIEW", "Watch or access lesson content");
        privilegeDefinitions.put("LESSON_MANAGE", "Create and manage lessons");
        privilegeDefinitions.put("TUTORIAL_VIEW", "Watch tutorial content");
        privilegeDefinitions.put("VIDEO_UPLOAD", "Upload video lessons and tutorials");
        privilegeDefinitions.put("LIVE_CLASS_JOIN", "Join live online classes");
        privilegeDefinitions.put("LIVE_CLASS_CONDUCT", "Conduct live online classes");
        privilegeDefinitions.put("DIGITAL_LIBRARY_VIEW", "Read or download materials from the digital library");
        privilegeDefinitions.put("DIGITAL_LIBRARY_MANAGE", "Upload and manage digital library resources");
        privilegeDefinitions.put("ASSIGNMENT_SUBMIT", "Submit assignments");
        privilegeDefinitions.put("ASSIGNMENT_MANAGE", "Create and manage assignments");
        privilegeDefinitions.put("QUIZ_EXAM_TAKE", "Take quizzes and exams");
        privilegeDefinitions.put("QUIZ_EXAM_MANAGE", "Create quizzes and exams");
        privilegeDefinitions.put("RESULT_VIEW", "View results and feedback");
        privilegeDefinitions.put("SUBMISSION_GRADE", "Grade student submissions");
        privilegeDefinitions.put("FEEDBACK_PROVIDE", "Provide feedback");
        privilegeDefinitions.put("CHAT_TEACHER", "Chat with teachers in lesson contexts");
        privilegeDefinitions.put("CHAT_STUDENT", "Chat with students");
        privilegeDefinitions.put("CHAT_PARENT", "Communicate with parents");
        privilegeDefinitions.put("ALERT_VIEW", "Receive notifications and alerts");
        privilegeDefinitions.put("HARMFUL_CONTENT_ALERT_VIEW", "Receive harmful content warnings");
        privilegeDefinitions.put("PROGRESS_VIEW_SELF", "Track personal progress");
        privilegeDefinitions.put("PROGRESS_VIEW_STUDENT", "View a student progress summary");
        privilegeDefinitions.put("AFAN_OROMO_MODULE_VIEW", "Access Afan Oromo language modules");
        privilegeDefinitions.put("BEHAVIOR_REPORT_CREATE", "Report behavioral issues");
        privilegeDefinitions.put("BEHAVIOR_REPORT_VIEW", "Monitor behavioral reports");
        privilegeDefinitions.put("ATTENDANCE_VIEW", "Monitor attendance information");
        privilegeDefinitions.put("ENGAGEMENT_VIEW", "Monitor learner engagement");
        privilegeDefinitions.put("PERFORMANCE_VIEW", "Monitor student performance");
        privilegeDefinitions.put("ASSIGNMENT_COMPLETION_VIEW", "Monitor assignment completion");
        privilegeDefinitions.put("USER_READ", "View users");
        privilegeDefinitions.put("USER_CREATE", "Create users");
        privilegeDefinitions.put("USER_UPDATE", "Update users");
        privilegeDefinitions.put("USER_DELETE", "Delete users");
        privilegeDefinitions.put("CLASS_ASSIGN", "Assign classes and subjects");
        privilegeDefinitions.put("ANNOUNCEMENT_MANAGE", "Manage announcements");
        privilegeDefinitions.put("COMPLIANCE_MANAGE", "Ensure system usage compliance");
        privilegeDefinitions.put("AI_INSIGHT_VIEW", "Access AI-generated insights");
        privilegeDefinitions.put("SCHOOL_REPORT_GENERATE", "Generate school-level reports");
        privilegeDefinitions.put("AGGREGATED_DATA_VIEW", "Access aggregated data across schools");
        privilegeDefinitions.put("SCHOOL_COMPARISON_VIEW", "Compare school performance");
        privilegeDefinitions.put("RISK_SCHOOL_IDENTIFY", "Identify high-risk schools and dropout trends");
        privilegeDefinitions.put("REGIONAL_REPORT_GENERATE", "Generate regional and strategic reports");
        privilegeDefinitions.put("POLICY_DECISION_SUPPORT", "Support policy decisions with data");
        privilegeDefinitions.put("ANALYTICS_PERFORMANCE", "Analyze student performance");
        privilegeDefinitions.put("ANALYTICS_ATTENDANCE", "Analyze attendance patterns");
        privilegeDefinitions.put("ANALYTICS_BEHAVIOR", "Analyze behavioral trends");
        privilegeDefinitions.put("PREDICT_DROPOUT_RISK", "Predict dropout risks");
        privilegeDefinitions.put("PREDICT_LOW_PERFORMANCE", "Predict low performance");
        privilegeDefinitions.put("GENERATE_REPORTS", "Generate reports");
        privilegeDefinitions.put("GENERATE_RECOMMENDATIONS", "Generate recommendations");
        privilegeDefinitions.put("ALERT_ABSENTEEISM", "Trigger absenteeism alerts");
        privilegeDefinitions.put("ALERT_BEHAVIOR_ISSUES", "Trigger behavior issue alerts");
        privilegeDefinitions.put("ALERT_HARMFUL_CONTENT", "Trigger harmful content alerts");
        privilegeDefinitions.put("LEARNING_PERSONALIZATION", "Personalize learning suggestions");
        privilegeDefinitions.put("SYSTEM_CONFIG_MANAGE", "Manage system configuration");
        privilegeDefinitions.put("ACCESS_ROLE_CONTROL", "Control access roles and permissions");
        privilegeDefinitions.put("SYSTEM_PERFORMANCE_MONITOR", "Monitor system performance");
        privilegeDefinitions.put("DATA_SECURITY_MANAGE", "Ensure data security");
        privilegeDefinitions.put("BACKUP_UPDATE_MANAGE", "Manage backups and updates");
        privilegeDefinitions.put("TECHNICAL_ISSUE_HANDLE", "Handle technical issues");
        privilegeDefinitions.put("AUTH_LOGIN", "Handle user login and logout");
        privilegeDefinitions.put("AUTH_RBAC", "Enforce role-based access control");
        privilegeDefinitions.put("AUTH_PASSWORD_MANAGE", "Manage passwords");
        privilegeDefinitions.put("AUTH_SECURITY_VALIDATE", "Perform security validation");
        privilegeDefinitions.put("ROLE_READ", "View roles");
        privilegeDefinitions.put("ROLE_CREATE", "Create roles");
        privilegeDefinitions.put("ROLE_UPDATE", "Update roles");
        privilegeDefinitions.put("ROLE_DELETE", "Delete roles");
        privilegeDefinitions.put("PRIVILEGE_READ", "View privileges");
        privilegeDefinitions.put("PRIVILEGE_CREATE", "Create privileges");
        privilegeDefinitions.put("PRIVILEGE_UPDATE", "Update privileges");
        privilegeDefinitions.put("PRIVILEGE_DELETE", "Delete privileges");

        Map<String, Privilege> privilegesByName = new LinkedHashMap<>();
        for (Map.Entry<String, String> definition : privilegeDefinitions.entrySet()) {
            Privilege privilege = privilegeRepository.findByNameIgnoreCase(definition.getKey())
                    .orElseGet(() -> privilegeRepository.save(new Privilege(definition.getKey(), definition.getValue())));
            if (!privilege.getDescription().equals(definition.getValue())) {
                privilege.setDescription(definition.getValue());
                privilege = privilegeRepository.save(privilege);
            }
            privilegesByName.put(privilege.getName(), privilege);
        }

        ensureRole("SYSTEM_ADMINISTRATOR", "Technical administrator responsible for platform configuration, security, and RBAC.", Set.of(
                "PROFILE_VIEW", "PROFILE_UPDATE", "USER_READ", "USER_CREATE", "USER_UPDATE", "USER_DELETE",
                "ROLE_READ", "ROLE_CREATE", "ROLE_UPDATE", "ROLE_DELETE",
                "PRIVILEGE_READ", "PRIVILEGE_CREATE", "PRIVILEGE_UPDATE", "PRIVILEGE_DELETE",
                "ACCESS_ROLE_CONTROL", "SYSTEM_CONFIG_MANAGE", "SYSTEM_PERFORMANCE_MONITOR",
                "DATA_SECURITY_MANAGE", "BACKUP_UPDATE_MANAGE", "TECHNICAL_ISSUE_HANDLE"
        ), privilegesByName);
        ensureRole("SCHOOL_ADMINISTRATOR", "School-level administrator responsible for managing users, classes, and compliance.", Set.of(
                "PROFILE_VIEW", "PROFILE_UPDATE", "USER_READ", "USER_CREATE", "USER_UPDATE", "CLASS_ASSIGN",
                "ATTENDANCE_VIEW", "BEHAVIOR_REPORT_VIEW", "PERFORMANCE_VIEW", "AI_INSIGHT_VIEW",
                "SCHOOL_REPORT_GENERATE", "COMPLIANCE_MANAGE", "ANNOUNCEMENT_MANAGE", "ALERT_VIEW"
        ), privilegesByName);
        ensureRole("TEACHER", "Teacher role responsible for creating courses, teaching, assessing, and monitoring students.", Set.of(
                "PROFILE_VIEW", "PROFILE_UPDATE", "COURSE_VIEW", "COURSE_MANAGE", "LESSON_VIEW", "LESSON_MANAGE",
                "TUTORIAL_VIEW", "VIDEO_UPLOAD", "LIVE_CLASS_CONDUCT", "DIGITAL_LIBRARY_MANAGE",
                "ASSIGNMENT_MANAGE", "QUIZ_EXAM_MANAGE", "SUBMISSION_GRADE", "FEEDBACK_PROVIDE",
                "CHAT_STUDENT", "CHAT_PARENT", "BEHAVIOR_REPORT_CREATE", "PERFORMANCE_VIEW",
                "ATTENDANCE_VIEW", "ENGAGEMENT_VIEW", "ALERT_VIEW"
        ), privilegesByName);
        ensureRole("STUDENT", "Primary learner role for course participation and academic progress.", Set.of(
                "PROFILE_VIEW", "PROFILE_UPDATE", "COURSE_ENROLL", "COURSE_VIEW", "LESSON_VIEW",
                "TUTORIAL_VIEW", "LIVE_CLASS_JOIN", "DIGITAL_LIBRARY_VIEW", "ASSIGNMENT_SUBMIT",
                "QUIZ_EXAM_TAKE", "RESULT_VIEW", "CHAT_TEACHER", "ALERT_VIEW",
                "HARMFUL_CONTENT_ALERT_VIEW", "PROGRESS_VIEW_SELF", "AFAN_OROMO_MODULE_VIEW"
        ), privilegesByName);
        ensureRole("PARENT", "Parent role for monitoring and supporting a student.", Set.of(
                "PROFILE_VIEW", "PROGRESS_VIEW_STUDENT", "ATTENDANCE_VIEW", "ASSIGNMENT_COMPLETION_VIEW",
                "ALERT_VIEW", "CHAT_TEACHER", "BEHAVIOR_REPORT_VIEW", "ENGAGEMENT_VIEW"
        ), privilegesByName);
        ensureRole("BUREAU_OF_EDUCATION", "Government oversight role for regional analytics and policy decisions.", Set.of(
                "PROFILE_VIEW", "AGGREGATED_DATA_VIEW", "ATTENDANCE_VIEW", "BEHAVIOR_REPORT_VIEW",
                "PERFORMANCE_VIEW", "SCHOOL_COMPARISON_VIEW", "RISK_SCHOOL_IDENTIFY",
                "REGIONAL_REPORT_GENERATE", "POLICY_DECISION_SUPPORT", "AI_INSIGHT_VIEW"
        ), privilegesByName);
        ensureRole("AI_SYSTEM", "Automated analysis and alerting role for the intelligent support engine.", Set.of(
                "ANALYTICS_PERFORMANCE", "ANALYTICS_ATTENDANCE", "ANALYTICS_BEHAVIOR",
                "PREDICT_DROPOUT_RISK", "PREDICT_LOW_PERFORMANCE", "GENERATE_REPORTS",
                "GENERATE_RECOMMENDATIONS", "ALERT_ABSENTEEISM", "ALERT_BEHAVIOR_ISSUES",
                "ALERT_HARMFUL_CONTENT", "LEARNING_PERSONALIZATION"
        ), privilegesByName);
        ensureRole("AUTHENTICATION_SYSTEM", "Supporting security role for identity and access management.", Set.of(
                "AUTH_LOGIN", "AUTH_RBAC", "AUTH_PASSWORD_MANAGE", "AUTH_SECURITY_VALIDATE"
        ), privilegesByName);

        for (User user : userRepository.findAll()) {
            if (user.getRole() == null) {
                continue;
            }
            String canonicalRoleName = user.getRole().canonical().name();
            AppRole mappedRole = appRoleRepository.findByNameIgnoreCase(canonicalRoleName)
                    .orElseThrow(() -> new RuntimeException("Missing bootstrap role: " + canonicalRoleName));
            boolean assigned = user.getAccessRoles().stream()
                    .anyMatch(role -> role.getName().equalsIgnoreCase(canonicalRoleName));
            if (!assigned) {
                user.addAccessRole(mappedRole);
                userRepository.save(user);
            }
        }
    }

    private void ensureRole(String roleName, String description, Set<String> privilegeNames, Map<String, Privilege> privilegesByName) {
        AppRole appRole = appRoleRepository.findByNameIgnoreCase(roleName)
                .orElseGet(() -> appRoleRepository.save(new AppRole(roleName, description)));
        appRole.setDescription(description);

        Set<Privilege> desiredPrivileges = new LinkedHashSet<>();
        for (String privilegeName : privilegeNames) {
            Privilege privilege = privilegesByName.get(privilegeName);
            if (privilege != null) {
                desiredPrivileges.add(privilege);
            }
        }

        for (Privilege existingPrivilege : new LinkedHashSet<>(appRole.getPrivileges())) {
            if (!desiredPrivileges.contains(existingPrivilege)) {
                appRole.removePrivilege(existingPrivilege);
            }
        }
        for (Privilege desiredPrivilege : desiredPrivileges) {
            if (!appRole.getPrivileges().contains(desiredPrivilege)) {
                appRole.addPrivilege(desiredPrivilege);
            }
        }
        appRoleRepository.save(appRole);
    }
}
