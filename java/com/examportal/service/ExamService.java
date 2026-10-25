package com.examportal.service;

import com.examportal.dto.ExamDto;
import com.examportal.model.Exam;

import java.util.List;

/**
 * Service interface for examination creation, configuration, and publishing.
 */
public interface ExamService {

    List<Exam> getExamsByTeacher(Long teacherId);

    Exam getExamByIdAndTeacher(Long examId, Long teacherId);

    ExamDto getExamDtoByIdAndTeacher(Long examId, Long teacherId);

    Long createExam(ExamDto dto, Long teacherId);

    void updateExam(Long examId, ExamDto dto, Long teacherId);

    void deleteExam(Long examId, Long teacherId);

    void togglePublish(Long examId, Long teacherId);

    void updateExamQuestions(Long examId, List<Long> questionIds, Long teacherId);

    boolean hasAttempts(Long examId);

    List<Exam> getPublishedExamsForStudent(Long studentId);

    long getExamCountByTeacher(Long teacherId);
}
