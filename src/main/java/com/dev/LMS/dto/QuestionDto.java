package com.dev.LMS.dto;

import com.dev.LMS.model.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionDto {

    private Long id;
    private QuestionType type;
    private String content;
    private List<Choice> choices;


    public static QuestionDto toDto(Question question){
        List<Choice> detachedChoices = new ArrayList<>();
        if (question.getChoices() != null) {
            detachedChoices = question.getChoices().stream()
                    .map(choice -> {
                        Choice c = new Choice();
                        c.setId(choice.getId());
                        c.setValue(choice.getValue());
                        return c;
                    })
                    .collect(Collectors.toList());
        }

        return QuestionDto.builder().
                choices(detachedChoices).
                id(question.getId()).
                content(question.getContent()).
                type(question.getType()).
                build();
    }public static List<QuestionDto> listToDto(List<Question> questions){
        List<QuestionDto> questionDtos = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            questionDtos.add(toDto(questions.get(i)));
        }
        return questionDtos;
    }

}
