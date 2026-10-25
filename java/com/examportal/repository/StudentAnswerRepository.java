package com.examportal.repository;

import com.examportal.model.ExamAttempt;
import com.examportal.model.Question;
import com.examportal.model.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for StudentAnswer entity.
 */
@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    List<StudentAnswer> findByAttempt(ExamAttempt attempt);

    List<StudentAnswer> findByAttemptId(Long attemptId);

    Optional<StudentAnswer> findByAttemptAndQuestion(ExamAttempt attempt, Question question);

    Optional<StudentAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);
}
