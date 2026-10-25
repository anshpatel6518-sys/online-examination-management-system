package com.examportal.repository;

import com.examportal.model.Course;
import com.examportal.model.Exam;
import com.examportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Exam entity.
 */
@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByCourse(Course course);

    List<Exam> findByCourseId(Long courseId);

    long countByCourseId(Long courseId);

    List<Exam> findByCreatedBy(User createdBy);

    List<Exam> findByCreatedById(Long createdById);

    List<Exam> findByPublishedTrue();

    List<Exam> findByCourseIdAndPublishedTrue(Long courseId);

    boolean existsByCourseId(Long courseId);

    List<Exam> findByCourseTeacherIdOrderByCreatedAtDesc(Long teacherId);

    java.util.Optional<Exam> findByIdAndCourseTeacherId(Long id, Long teacherId);

    long countByCourseTeacherId(Long teacherId);

    List<Exam> findByCourseIdInAndPublishedTrue(List<Long> courseIds);
}
