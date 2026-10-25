package com.examportal.service.impl;

import com.examportal.dto.ExamDto;
import com.examportal.model.Course;
import com.examportal.model.Exam;
import com.examportal.model.Question;
import com.examportal.model.User;
import com.examportal.repository.CourseRepository;
import com.examportal.repository.ExamAttemptRepository;
import com.examportal.repository.ExamRepository;
import com.examportal.repository.QuestionRepository;
import com.examportal.repository.UserRepository;
import com.examportal.service.ExamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service implementation for teacher exam lifecycle management.
 */
@Service
@Transactional
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final UserRepository userRepository;

    public ExamServiceImpl(ExamRepository examRepository,
                           CourseRepository courseRepository,
                           QuestionRepository questionRepository,
                           ExamAttemptRepository examAttemptRepository,
                           UserRepository userRepository) {
        this.examRepository = examRepository;
        this.courseRepository = courseRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exam> getExamsByTeacher(Long teacherId) {
        return examRepository.findByCourseTeacherIdOrderByCreatedAtDesc(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public Exam getExamByIdAndTeacher(Long examId, Long teacherId) {
        return examRepository.findByIdAndCourseTeacherId(examId, teacherId)
                .orElseThrow(() -> new SecurityException("Exam not found or you do not have permission to manage it."));
    }

    @Override
    @Transactional(readOnly = true)
    public ExamDto getExamDtoByIdAndTeacher(Long examId, Long teacherId) {
        Exam exam = getExamByIdAndTeacher(examId, teacherId);

        ExamDto dto = new ExamDto();
        dto.setId(exam.getId());
        dto.setTitle(exam.getTitle());
        dto.setCourseId(exam.getCourse().getId());
        dto.setDurationMinutes(exam.getDurationMinutes());
        dto.setPassingMarks(exam.getPassingMarks());
        dto.setStartTime(exam.getStartTime());
        dto.setEndTime(exam.getEndTime());
        dto.setNegativeMarking(exam.isNegativeMarking());
        dto.setNegativeMarkValue(exam.getNegativeMarkValue());
        dto.setShuffleQuestions(exam.isShuffleQuestions());
        dto.setShowAnswerKey(exam.isShowAnswerKey());

        return dto;
    }

    @Override
    public Long createExam(ExamDto dto, Long teacherId) {
        Course course = verifyCourseOwnership(dto.getCourseId(), teacherId);
        validateExamSchedule(dto);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalStateException("Teacher not found."));

        Exam exam = new Exam();
        exam.setTitle(dto.getTitle().trim());
        exam.setCourse(course);
        exam.setCreatedBy(teacher);
        exam.setDurationMinutes(dto.getDurationMinutes());
        exam.setPassingMarks(dto.getPassingMarks());
        exam.setTotalMarks(0);
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setNegativeMarking(dto.isNegativeMarking());
        exam.setNegativeMarkValue(dto.isNegativeMarking() ? dto.getNegativeMarkValue() : 0.0);
        exam.setShuffleQuestions(dto.isShuffleQuestions());
        exam.setShowAnswerKey(dto.isShowAnswerKey());
        exam.setPublished(false);

        Exam saved = examRepository.save(exam);
        return saved.getId();
    }

    @Override
    public void updateExam(Long examId, ExamDto dto, Long teacherId) {
        Exam exam = getExamByIdAndTeacher(examId, teacherId);
        Course course = verifyCourseOwnership(dto.getCourseId(), teacherId);
        validateExamSchedule(dto);

        // If exam course is changed, verify it has no attempts
        if (!exam.getCourse().getId().equals(course.getId()) && hasAttempts(examId)) {
            throw new IllegalStateException("Cannot change the course of an exam that has recorded student attempts.");
        }

        exam.setTitle(dto.getTitle().trim());
        exam.setCourse(course);
        exam.setDurationMinutes(dto.getDurationMinutes());
        exam.setPassingMarks(dto.getPassingMarks());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setNegativeMarking(dto.isNegativeMarking());
        exam.setNegativeMarkValue(dto.isNegativeMarking() ? dto.getNegativeMarkValue() : 0.0);
        exam.setShuffleQuestions(dto.isShuffleQuestions());
        exam.setShowAnswerKey(dto.isShowAnswerKey());

        examRepository.save(exam);
    }

    @Override
    public void deleteExam(Long examId, Long teacherId) {
        Exam exam = getExamByIdAndTeacher(examId, teacherId);

        if (hasAttempts(examId)) {
            throw new IllegalStateException("Cannot delete an exam that has already been attempted by students.");
        }

        examRepository.delete(exam);
    }

    @Override
    public void togglePublish(Long examId, Long teacherId) {
        Exam exam = getExamByIdAndTeacher(examId, teacherId);

        if (!exam.isPublished()) {
            // Publishing validation
            if (exam.getQuestions().isEmpty()) {
                throw new IllegalStateException("Cannot publish an exam with no questions. Please select questions from the Question Bank first.");
            }
            if (exam.getPassingMarks() > exam.getTotalMarks()) {
                throw new IllegalStateException("Passing marks (" + exam.getPassingMarks() + ") cannot exceed total exam marks (" + exam.getTotalMarks() + ").");
            }
            exam.setPublished(true);
        } else {
            exam.setPublished(false);
        }

        examRepository.save(exam);
    }

    @Override
    public void updateExamQuestions(Long examId, List<Long> questionIds, Long teacherId) {
        Exam exam = getExamByIdAndTeacher(examId, teacherId);

        // Strict rule: A published exam cannot have its questions changed once any student has attempted it
        if (hasAttempts(examId)) {
            throw new IllegalStateException("A published exam cannot have its questions changed once any student has attempted it.");
        }

        List<Question> selectedQuestions = new ArrayList<>();
        int calculatedTotalMarks = 0;

        if (questionIds != null && !questionIds.isEmpty()) {
            List<Question> candidateQuestions = questionRepository.findAllById(questionIds);
            for (Question q : candidateQuestions) {
                // Verify each question belongs to the exam's course
                if (q.getCourse() != null && q.getCourse().getId().equals(exam.getCourse().getId())) {
                    selectedQuestions.add(q);
                    calculatedTotalMarks += (q.getMarks() != null ? q.getMarks() : 1);
                }
            }
        }

        exam.setQuestions(selectedQuestions);
        exam.setTotalMarks(calculatedTotalMarks);

        examRepository.save(exam);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAttempts(Long examId) {
        return examAttemptRepository.countByExamId(examId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exam> getPublishedExamsForStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalStateException("Student not found."));

        List<Long> enrolledCourseIds = new ArrayList<>();
        for (Course c : student.getEnrolledCourses()) {
            enrolledCourseIds.add(c.getId());
        }

        if (enrolledCourseIds.isEmpty()) {
            return Collections.emptyList();
        }

        return examRepository.findByCourseIdInAndPublishedTrue(enrolledCourseIds);
    }

    @Override
    @Transactional(readOnly = true)
    public long getExamCountByTeacher(Long teacherId) {
        return examRepository.countByCourseTeacherId(teacherId);
    }

    private Course verifyCourseOwnership(Long courseId, Long teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));

        if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacherId)) {
            throw new SecurityException("You can only configure exams for courses assigned to you.");
        }

        return course;
    }

    private void validateExamSchedule(ExamDto dto) {
        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            if (!dto.getEndTime().isAfter(dto.getStartTime())) {
                throw new IllegalArgumentException("End date/time must be scheduled after start date/time.");
            }
        }
    }
}
