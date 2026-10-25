package com.examportal.service.impl;

import com.examportal.dto.CourseDto;
import com.examportal.model.Course;
import com.examportal.model.User;
import com.examportal.repository.CourseRepository;
import com.examportal.repository.ExamRepository;
import com.examportal.repository.UserRepository;
import com.examportal.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for course management.
 */
@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;

    public CourseServiceImpl(CourseRepository courseRepository,
                             UserRepository userRepository,
                             ExamRepository examRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.examRepository = examRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getCourseDtoById(Long id) {
        Course course = getCourseById(id);
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setCode(course.getCode());
        dto.setDescription(course.getDescription());
        if (course.getTeacher() != null) {
            dto.setTeacherId(course.getTeacher().getId());
        }
        return dto;
    }

    @Override
    public void createCourse(CourseDto dto) {
        String code = dto.getCode().trim().toUpperCase();
        if (courseRepository.existsByCode(code)) {
            throw new IllegalArgumentException("A course with code '" + code + "' already exists.");
        }

        Course course = new Course();
        course.setName(dto.getName().trim());
        course.setCode(code);
        course.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);

        if (dto.getTeacherId() != null) {
            User teacher = userRepository.findById(dto.getTeacherId()).orElse(null);
            course.setTeacher(teacher);
        }

        courseRepository.save(course);
    }

    @Override
    public void updateCourse(Long id, CourseDto dto) {
        Course course = getCourseById(id);
        String code = dto.getCode().trim().toUpperCase();

        if (courseRepository.existsByCodeAndIdNot(code, id)) {
            throw new IllegalArgumentException("A course with code '" + code + "' already exists.");
        }

        course.setName(dto.getName().trim());
        course.setCode(code);
        course.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);

        if (dto.getTeacherId() != null) {
            User teacher = userRepository.findById(dto.getTeacherId()).orElse(null);
            course.setTeacher(teacher);
        } else {
            course.setTeacher(null);
        }

        courseRepository.save(course);
    }

    @Override
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);

        if (examRepository.existsByCourseId(id)) {
            throw new IllegalStateException("Cannot delete course because it has associated exams. Delete the exams first.");
        }

        courseRepository.delete(course);
    }
}
