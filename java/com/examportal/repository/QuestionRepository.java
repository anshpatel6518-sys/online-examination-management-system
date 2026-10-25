package com.examportal.repository;

import com.examportal.model.Course;
import com.examportal.model.Exam;
import com.examportal.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Question entity.
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByExam(Exam exam);

    List<Question> findByExamId(Long examId);

    List<Question> findByCourse(Course course);

    List<Question> findByCourseId(Long courseId);

    long countByExamId(Long examId);

    long countByCourseTeacherId(Long teacherId);

    java.util.Optional<Question> findByIdAndCourseTeacherId(Long id, Long teacherId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT q FROM Question q WHERE q.course.teacher.id = :teacherId " +
        "AND (:courseId IS NULL OR q.course.id = :courseId) " +
        "AND (:difficulty IS NULL OR q.difficulty = :difficulty)"
    )
    org.springframework.data.domain.Page<Question> findByTeacherAndFilters(
            @org.springframework.data.repository.query.Param("teacherId") Long teacherId,
            @org.springframework.data.repository.query.Param("courseId") Long courseId,
            @org.springframework.data.repository.query.Param("difficulty") com.examportal.model.Difficulty difficulty,
            org.springframework.data.domain.Pageable pageable
    );
}
