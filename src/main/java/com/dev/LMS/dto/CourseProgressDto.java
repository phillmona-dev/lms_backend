package com.dev.LMS.dto;

import com.dev.LMS.model.Course;

public class CourseProgressDto {
    private long courseId;
    private String courseName;
    private String instructorName;
    private int totalLessons;
    private int attendedLessons;
    private int totalAssignments;
    private int submittedAssignments;
    private int totalQuizzes;
    private int attemptedQuizzes;

    public CourseProgressDto(Course course,
                             int totalLessons,
                             int attendedLessons,
                             int totalAssignments,
                             int submittedAssignments,
                             int totalQuizzes,
                             int attemptedQuizzes) {
        this.courseId = course.getCourseId();
        this.courseName = course.getName();
        this.instructorName = course.getInstructor() == null ? null : course.getInstructor().getName();
        this.totalLessons = totalLessons;
        this.attendedLessons = attendedLessons;
        this.totalAssignments = totalAssignments;
        this.submittedAssignments = submittedAssignments;
        this.totalQuizzes = totalQuizzes;
        this.attemptedQuizzes = attemptedQuizzes;
    }

    public long getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public int getAttendedLessons() {
        return attendedLessons;
    }

    public int getTotalAssignments() {
        return totalAssignments;
    }

    public int getSubmittedAssignments() {
        return submittedAssignments;
    }

    public int getTotalQuizzes() {
        return totalQuizzes;
    }

    public int getAttemptedQuizzes() {
        return attemptedQuizzes;
    }
}
