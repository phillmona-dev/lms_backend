package com.dev.LMS.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="quiz")
public class Quiz {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long quizID;
    @ManyToOne( cascade = CascadeType.PERSIST)
    @JoinColumn(name= "course_id")
    private Course course;

    @Column(nullable = false, unique = true)
    private String quizTitle;

    private String quizDuration;

    @OneToMany(mappedBy ="quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizSubmission> submissions = new ArrayList<>();
    
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "quiz_questions",
        joinColumns = @JoinColumn(name = "quiz_id"),
        inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions = new ArrayList<>();
    
    public void addQuizSubmission(QuizSubmission quizSubmission){
        if (this.submissions == null) {
            this.submissions = new ArrayList<>();
        }
        this.submissions.add(quizSubmission);
        quizSubmission.setQuiz(this);
    }
    
    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    public Long getQuizID() {
        return this.quizID;
    }

    public void setQuizID(Long quizID) {
        this.quizID = quizID;
    }

    public Course getCourseId() {
        return this.course;
    }

    public void setCourseId(Course courseId) {
        this.course = courseId;
    }

    public String getQuizTitle() {
        return this.quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public String getQuizDuration() {
        return this.quizDuration;
    }

    public void setQuizDuration(String quizDuration) {
        this.quizDuration = quizDuration;
    }

    public List<QuizSubmission> getSubmissions() {
        return this.submissions;
    }

    public void setSubmissions(List<QuizSubmission> submissions) {
        this.submissions = submissions;
    }

    public Course getCourse() {
        return this.course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
    
    public List<Question> getQuestions() {
        return this.questions;
    }
    
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
    
    public QuizSubmission findbyStudent(Student student){
        if (this.getSubmissions() == null || student == null) {
            return null;
        }
        for (int i = 0; i < this.getSubmissions().size(); i++){
            if((this.getSubmissions().get(i).getStudent()).equals(student))
                return this.getSubmissions().get(i);
        }
        return null;
    }
}
