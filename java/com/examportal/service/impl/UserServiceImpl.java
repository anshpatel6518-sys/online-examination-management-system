package com.examportal.service.impl;

import com.examportal.dto.AdminStatsDto;
import com.examportal.dto.CreateTeacherDto;
import com.examportal.model.Course;
import com.examportal.model.Role;
import com.examportal.model.User;
import com.examportal.repository.CourseRepository;
import com.examportal.repository.ExamRepository;
import com.examportal.repository.UserRepository;
import com.examportal.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for user administration.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ExamRepository examRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           CourseRepository courseRepository,
                           ExamRepository examRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.examRepository = examRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsDto getAdminStats() {
        long students = userRepository.countByRole(Role.STUDENT);
        long teachers = userRepository.countByRole(Role.TEACHER);
        long courses = courseRepository.count();
        long exams = examRepository.count();

        return new AdminStatsDto(students, teachers, courses, exams);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (search != null && !search.trim().isEmpty()) {
            String query = search.trim();
            return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, pageable);
        }

        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Override
    public void createTeacher(CreateTeacherDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("A user with this email address already exists.");
        }

        User teacher = new User();
        teacher.setFullName(dto.getFullName().trim());
        teacher.setEmail(email);
        teacher.setPassword(passwordEncoder.encode(dto.getPassword()));
        teacher.setRole(Role.TEACHER);
        teacher.setEnabled(true);

        userRepository.save(teacher);
    }

    @Override
    public void toggleUserStatus(Long userId, String currentAdminEmail) {
        User user = getUserById(userId);

        if (user.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new IllegalArgumentException("You cannot disable your own administrator account.");
        }

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId, String currentAdminEmail) {
        User user = getUserById(userId);

        if (user.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new IllegalArgumentException("You cannot delete your own administrator account.");
        }

        // Unlink courses taught by this teacher so courses are preserved
        if (user.getRole() == Role.TEACHER) {
            List<Course> courses = courseRepository.findByTeacherId(user.getId());
            for (Course course : courses) {
                course.setTeacher(null);
                courseRepository.save(course);
            }
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllTeachers() {
        return userRepository.findByRoleAndEnabledTrue(Role.TEACHER);
    }

    @Override
    @Transactional(readOnly = true)
    public com.examportal.dto.EnrollmentDto getStudentEnrollmentDto(Long studentId) {
        User student = getUserById(studentId);
        List<Long> enrolledCourseIds = new java.util.ArrayList<>();
        for (Course c : student.getEnrolledCourses()) {
            enrolledCourseIds.add(c.getId());
        }
        return new com.examportal.dto.EnrollmentDto(
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                enrolledCourseIds
        );
    }

    @Override
    public void updateStudentEnrollments(Long studentId, List<Long> courseIds) {
        User student = getUserById(studentId);
        List<Course> courses = new java.util.ArrayList<>();
        if (courseIds != null && !courseIds.isEmpty()) {
            courses = courseRepository.findAllById(courseIds);
        }
        student.setEnrolledCourses(courses);
        userRepository.save(student);
    }
}
