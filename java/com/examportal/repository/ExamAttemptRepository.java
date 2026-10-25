package com.examportal.repository;

import com.examportal.model.AttemptStatus;
import com.examportal.model.Exam;
import com.examportal.model.ExamAttempt;
import com.examportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ExamAttempt entity.
 */
@Repository
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {

    List<ExamAttempt> findByStudent(User student);

    List<ExamAttempt> findByStudentId(Long studentId);

    List<ExamAttempt> findByExam(Exam exam);

    List<ExamAttempt> findByExamId(Long examId);

    List<ExamAttempt> findByStudentIdAndExamId(Long studentId, Long examId);

    Optional<ExamAttempt> findByStudentIdAndExamIdAndStatus(Long studentId, Long examId, AttemptStatus status);

    boolean existsByStudentIdAndExamIdAndStatus(Long studentId, Long examId, AttemptStatus status);

    long countByExamId(Long examId);
}
