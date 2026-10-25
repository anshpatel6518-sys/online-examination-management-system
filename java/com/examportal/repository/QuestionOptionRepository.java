package com.examportal.repository;

import com.examportal.model.Question;
import com.examportal.model.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for QuestionOption entity.
 */
@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    List<QuestionOption> findByQuestion(Question question);

    List<QuestionOption> findByQuestionId(Long questionId);

    Optional<QuestionOption> findByQuestionIdAndCorrectTrue(Long questionId);
}
