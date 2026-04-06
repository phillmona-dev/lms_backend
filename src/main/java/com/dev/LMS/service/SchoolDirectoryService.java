package com.dev.LMS.service;

import com.dev.LMS.dto.CourseSchoolAssignmentRequestDto;
import com.dev.LMS.dto.SchoolDto;
import com.dev.LMS.dto.SchoolRequestDto;
import com.dev.LMS.dto.UserSchoolAssignmentRequestDto;
import com.dev.LMS.model.Course;
import com.dev.LMS.model.Role;
import com.dev.LMS.model.School;
import com.dev.LMS.model.User;
import com.dev.LMS.repository.CourseRepository;
import com.dev.LMS.repository.SchoolRepository;
import com.dev.LMS.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolDirectoryService {
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;

    public SchoolDirectoryService(SchoolRepository schoolRepository,
                                  UserRepository userRepository,
                                  CourseRepository courseRepository,
                                  UserService userService) {
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.userService = userService;
    }

    public SchoolDto createSchool(String email, SchoolRequestDto requestDto) {
        ensureSystemAdmin(email);
        School school = new School();
        school.setName(requestDto.getName().trim());
        school.setRegion(requestDto.getRegion().trim());
        school.setCode(requestDto.getCode().trim().toUpperCase());
        return new SchoolDto(schoolRepository.save(school));
    }

    public List<SchoolDto> getSchools(String email) {
        ensureAuthenticated(email);
        return schoolRepository.findAll().stream().map(SchoolDto::new).toList();
    }

    public SchoolDto assignUserToSchool(String email, UserSchoolAssignmentRequestDto requestDto) {
        ensureSystemAdmin(email);
        User user = userService.getUserByPublicId(requestDto.getUserId());
        School school = getSchool(requestDto.getSchoolId());
        user.setSchool(school);
        return new SchoolDto(userRepository.save(user).getSchool());
    }

    public SchoolDto assignCourseToSchool(String email, CourseSchoolAssignmentRequestDto requestDto) {
        ensureSystemAdmin(email);
        Course course = courseRepository.findById(requestDto.getCourseId())
                .orElseThrow(() -> new IllegalStateException("Course not found."));
        School school = getSchool(requestDto.getSchoolId());
        course.setSchool(school);
        return new SchoolDto(courseRepository.save(course).getSchool());
    }

    private void ensureSystemAdmin(String email) {
        User user = userService.getUserByEmail(email);
        if (user.getRole() == null || user.getRole().canonical() != Role.SYSTEM_ADMINISTRATOR) {
            throw new IllegalStateException("Only system administrators can manage schools.");
        }
    }

    private void ensureAuthenticated(String email) {
        userService.getUserByEmail(email);
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalStateException("School not found."));
    }
}
