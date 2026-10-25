package com.examportal.repository;

import com.examportal.model.ExamAttempt;
import com.examportal.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Result entity.
 */
@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    Optional<Result> findByAttempt(ExamAttempt attempt);

    Optional<Result> findByAttemptId(Long attemptId);

    List<Result> findByAttemptExamId(Long examId);

    List<Result> findByAttemptExamIdOrderByScoreDesc(Long examId);

    List<Result> findByAttemptStudentId(Long studentId);

    List<Result> findByAttemptStudentIdOrderByCreatedAtDesc(Long studentId);
}
