package com.examportal.service;

import com.examportal.dto.AdminStatsDto;
import com.examportal.dto.CreateTeacherDto;
import com.examportal.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for user operations and administrative management.
 */
public interface UserService {

    AdminStatsDto getAdminStats();

    Page<User> getUsers(String search, int page, int size);

    User getUserById(Long id);

    void createTeacher(CreateTeacherDto dto);

    void toggleUserStatus(Long userId, String currentAdminEmail);

    void deleteUser(Long userId, String currentAdminEmail);

    List<User> getAllTeachers();

    com.examportal.dto.EnrollmentDto getStudentEnrollmentDto(Long studentId);

    void updateStudentEnrollments(Long studentId, List<Long> courseIds);
}
