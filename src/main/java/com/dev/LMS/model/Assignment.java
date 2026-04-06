package com.dev.LMS.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int assignment_id;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private LocalDateTime dueDate;  // "2024-12-20T10:15:30"

    @Column(name = "attachment_file_name")
    private String attachmentFileName;

    @Column(name = "attachment_file_type")
    private String attachmentFileType;

    @Column(name = "attachment_file_path")
    private String attachmentFilePath;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Course course;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<AssignmentSubmission> submissions = new ArrayList<>();

    public int getAssignmentId() {
        return assignment_id;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignment_id = assignmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getAttachmentFileName() {
        return attachmentFileName;
    }

    public void setAttachmentFileName(String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
    }

    public String getAttachmentFileType() {
        return attachmentFileType;
    }

    public void setAttachmentFileType(String attachmentFileType) {
        this.attachmentFileType = attachmentFileType;
    }

    public String getAttachmentFilePath() {
        return attachmentFilePath;
    }

    public void setAttachmentFilePath(String attachmentFilePath) {
        this.attachmentFilePath = attachmentFilePath;
    }

    public List<AssignmentSubmission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<AssignmentSubmission> submissions) {
        this.submissions = submissions;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return Objects.equals(assignment_id, that.assignment_id)
                && Objects.equals(title, that.title)
                && Objects.equals(description, that.description)
                && Objects.equals(dueDate, that.dueDate)
                && Objects.equals(attachmentFileName, that.attachmentFileName)
                && Objects.equals(attachmentFileType, that.attachmentFileType)
                && Objects.equals(attachmentFilePath, that.attachmentFilePath)
                && Objects.equals(course, that.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignment_id, title, description, dueDate, attachmentFileName, attachmentFileType, attachmentFilePath, course);
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "assignmentId=" + assignment_id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", attachmentFileName='" + attachmentFileName + '\'' +
                ", attachmentFileType='" + attachmentFileType + '\'' +
                ", attachmentFilePath='" + attachmentFilePath + '\'' +
                ", course=" + course +
                '}';
    }
}
