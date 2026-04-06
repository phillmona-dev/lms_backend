package com.dev.LMS.dto;

import com.dev.LMS.model.Course;
import com.dev.LMS.model.Question;
import com.dev.LMS.model.Quiz;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class QuizDto {
    private Long quizID;
    private String quizTitle;
    private String quizDuration;
    private String courseName;
    private Integer questionCount;
    private Integer submissionCount;
    private List<QuestionDto> questions;

public static QuizDto toDto(Quiz quiz){
    int subCount = quiz.getSubmissions() != null ? quiz.getSubmissions().size() : 0;
    List<Question> quizQuestions = quiz.getQuestions();
    int qCount = quizQuestions != null ? quizQuestions.size() : 0;
    List<QuestionDto> questionDtos = quizQuestions != null ? QuestionDto.listToDto(quizQuestions) : new ArrayList<>();
    return QuizDto.builder().
            quizID(quiz.getQuizID()).
            quizDuration(quiz.getQuizDuration()).
            quizTitle(quiz.getQuizTitle()).
            courseName(quiz.getCourse() != null ? quiz.getCourse().getName() : null).
            questionCount(qCount).
            submissionCount(subCount).
            questions(questionDtos).
            build();
}

}
