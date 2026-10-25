package com.examportal.repository;

import com.examportal.model.Course;
import com.examportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Course entity.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Course> findAllByOrderByNameAsc();

    List<Course> findByTeacher(User teacher);

    List<Course> findByTeacherId(Long teacherId);
}
