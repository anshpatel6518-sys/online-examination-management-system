package com.examportal.service;

import com.examportal.dto.BulkUploadResultDto;
import com.examportal.dto.QuestionDto;
import com.examportal.model.Difficulty;
import com.examportal.model.Question;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for Question Bank operations and CSV imports.
 */
public interface QuestionService {

    Page<Question> getQuestionsByTeacher(Long teacherId, Long courseId, Difficulty difficulty, int page, int size);

    Question getQuestionByIdAndTeacher(Long questionId, Long teacherId);

    QuestionDto getQuestionDtoByIdAndTeacher(Long questionId, Long teacherId);

    void createQuestion(QuestionDto dto, Long teacherId);

    void updateQuestion(Long questionId, QuestionDto dto, Long teacherId);

    void deleteQuestion(Long questionId, Long teacherId);

    BulkUploadResultDto bulkUploadQuestions(MultipartFile file, Long courseId, Long teacherId);

    long getQuestionCountByTeacher(Long teacherId);
}
