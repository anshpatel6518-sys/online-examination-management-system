package com.examportal.service;

import com.examportal.dto.CourseDto;
import com.examportal.model.Course;

import java.util.List;

/**
 * Service interface for academic courses.
 */
public interface CourseService {

    List<Course> getAllCourses();

    Course getCourseById(Long id);

    CourseDto getCourseDtoById(Long id);

    void createCourse(CourseDto dto);

    void updateCourse(Long id, CourseDto dto);

    void deleteCourse(Long id);
}
