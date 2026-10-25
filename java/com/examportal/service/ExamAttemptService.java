package com.examportal.service;

import com.examportal.dto.AutoSaveResponseDto;
import com.examportal.dto.SaveAnswerDto;
import com.examportal.dto.StudentExamItemDto;
import com.examportal.dto.TakeExamQuestionDto;
import com.examportal.model.Exam;
import com.examportal.model.ExamAttempt;

import java.util.List;

/**
 * Service interface for student examination attempts, session timers, and answer auto-saving.
 */
public interface ExamAttemptService {

    /**
     * Retrieve customized exam status items for the student portal view.
     */
    List<StudentExamItemDto> getStudentExamsView(Long studentId);

    /**
     * Retrieve exam details for pre-exam instructions page with enrollment & window validation.
     */
    Exam getExamForInstructions(Long examId, Long studentId);

    /**
     * Start a new attempt or resume an existing in-progress attempt.
     */
    ExamAttempt startOrResumeAttempt(Long examId, Long studentId);

    /**
     * Retrieve and validate an active in-progress attempt for taking the exam.
     */
    ExamAttempt getAttemptForTaking(Long attemptId, Long studentId);

    /**
     * Calculate remaining time in seconds on the server.
     */
    long calculateRemainingSeconds(ExamAttempt attempt);

    /**
     * Build the ordered list of question DTOs for the exam taking screen, preserving shuffled sequence.
     */
    List<TakeExamQuestionDto> buildTakeExamQuestions(ExamAttempt attempt);

    /**
     * Auto-save or clear a question response via AJAX.
     */
    AutoSaveResponseDto saveAnswer(Long attemptId, Long studentId, SaveAnswerDto dto);

    /**
     * Finalize and submit an exam attempt.
     */
    void submitAttempt(Long attemptId, Long studentId);

    /**
     * Retrieve submitted attempt summary for the completion screen.
     */
    ExamAttempt getSubmittedAttemptSummary(Long attemptId, Long studentId);

    /**
     * Record tab-switching event during an active exam attempt.
     */
    int recordTabSwitch(Long attemptId, Long studentId);
}
