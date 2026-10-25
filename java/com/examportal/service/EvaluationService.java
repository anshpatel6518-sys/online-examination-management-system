package com.examportal.service;

import com.examportal.dto.ExamResultsSummaryDto;
import com.examportal.dto.StudentResultViewDto;
import com.examportal.model.ExamAttempt;
import com.examportal.model.Result;

import java.util.List;

/**
 * Service interface for automated assessment evaluation, scoring, negative marking, and result reporting.
 */
public interface EvaluationService {

    /**
     * Automatically grade an exam attempt, apply negative marking if configured,
     * compute percentage and pass/fail status, and persist the Result.
     */
    Result evaluateAttempt(ExamAttempt attempt);

    /**
     * Retrieve individual student result scorecard and question review.
     */
    StudentResultViewDto getStudentResult(Long attemptId, Long studentId);

    /**
     * Retrieve complete examination result history for a student.
     */
    List<StudentResultViewDto> getStudentResultsHistory(Long studentId);

    /**
     * Retrieve teacher comprehensive examination results summary, analytics, and student rankings.
     */
    ExamResultsSummaryDto getTeacherExamResults(Long examId, Long teacherId);

    /**
     * Retrieve teacher detailed question-wise review for an individual student attempt.
     */
    StudentResultViewDto getTeacherStudentSubmissionDetail(Long examId, Long attemptId, Long teacherId);
}
