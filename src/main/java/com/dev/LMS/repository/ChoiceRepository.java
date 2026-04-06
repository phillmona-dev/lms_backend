package com.dev.LMS.repository;

import com.dev.LMS.model.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoiceRepository extends JpaRepository<Choice, Long> {
    @Modifying
    @Query(value = "DELETE FROM question_choices WHERE question_id = :questionId", nativeQuery = true)
    void deleteChoicesByQuestionId(@Param("questionId") Long questionId);
    
    @Modifying
    @Query(value = "DELETE FROM quiz_questions WHERE question_id = :questionId", nativeQuery = true)
    void deleteQuizQuestionsByQuestionId(@Param("questionId") Long questionId);
}
