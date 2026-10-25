package com.examportal.service.impl;

import com.examportal.dto.ExamResultsSummaryDto;
import com.examportal.dto.QuestionReviewDto;
import com.examportal.dto.StudentResultViewDto;
import com.examportal.dto.TeacherStudentResultItemDto;
import com.examportal.model.AttemptStatus;
import com.examportal.model.Exam;
import com.examportal.model.ExamAttempt;
import com.examportal.model.Question;
import com.examportal.model.QuestionOption;
import com.examportal.model.Result;
import com.examportal.model.StudentAnswer;
import com.examportal.repository.ExamAttemptRepository;
import com.examportal.repository.ExamRepository;
import com.examportal.repository.ResultRepository;
import com.examportal.repository.StudentAnswerRepository;
import com.examportal.service.EvaluationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for automated evaluation, score calculation, negative marking,
 * and academic result reporting.
 */
@Service
@Transactional
public class EvaluationServiceImpl implements EvaluationService {

    private final ResultRepository resultRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final ExamRepository examRepository;
    private final com.examportal.service.EmailService emailService;

    public EvaluationServiceImpl(ResultRepository resultRepository,
                                 ExamAttemptRepository examAttemptRepository,
                                 StudentAnswerRepository studentAnswerRepository,
                                 ExamRepository examRepository,
                                 com.examportal.service.EmailService emailService) {
        this.resultRepository = resultRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.examRepository = examRepository;
        this.emailService = emailService;
    }

    @Override
    public Result evaluateAttempt(ExamAttempt attempt) {
        // Return existing result if already evaluated
        Optional<Result> existingResult = resultRepository.findByAttemptId(attempt.getId());
        if (existingResult.isPresent()) {
            return existingResult.get();
        }

        Exam exam = attempt.getExam();
        List<StudentAnswer> answers = studentAnswerRepository.findByAttemptId(attempt.getId());
        Map<Long, StudentAnswer> answerMap = new HashMap<>();
        for (StudentAnswer sa : answers) {
            if (sa.getQuestion() != null) {
                answerMap.put(sa.getQuestion().getId(), sa);
            }
        }

        List<Question> questions = exam.getQuestions();
        double rawScore = 0.0;
        int correctCount = 0;
        int wrongCount = 0;
        int unattemptedCount = 0;
        double calculatedTotalMarks = (exam.getTotalMarks() != null && exam.getTotalMarks() > 0)
                ? exam.getTotalMarks()
                : 0.0;

        for (Question q : questions) {
            if (exam.getTotalMarks() == null || exam.getTotalMarks() <= 0) {
                calculatedTotalMarks += (q.getMarks() != null ? q.getMarks() : 1);
            }

            StudentAnswer sa = answerMap.get(q.getId());
            if (sa == null || sa.getSelectedOption() == null) {
                // Unattempted: 0 marks, no penalty
                unattemptedCount++;
            } else {
                QuestionOption selected = sa.getSelectedOption();
                if (selected.isCorrect()) {
                    correctCount++;
                    int qMarks = (q.getMarks() != null) ? q.getMarks() : 1;
                    rawScore += qMarks;
                } else {
                    wrongCount++;
                    if (exam.isNegativeMarking()) {
                        double penalty = (exam.getNegativeMarkValue() != null) ? exam.getNegativeMarkValue() : 0.25;
                        rawScore -= penalty;
                    }
                }
            }
        }

        // Floor negative score to 0.0 and round to 2 decimals
        double finalScore = Math.max(0.0, rawScore);
        finalScore = BigDecimal.valueOf(finalScore).setScale(2, RoundingMode.HALF_UP).doubleValue();

        double totalMarks = Math.max(1.0, calculatedTotalMarks);
        double percentage = (finalScore / totalMarks) * 100.0;
        percentage = BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP).doubleValue();

        int passingMarks = (exam.getPassingMarks() != null) ? exam.getPassingMarks() : 40;
        boolean passed = (finalScore >= passingMarks);

        Result result = new Result();
        result.setAttempt(attempt);
        result.setScore(finalScore);
        result.setTotalMarks(totalMarks);
        result.setPercentage(percentage);
        result.setPassed(passed);
        result.setCorrectCount(correctCount);
        result.setWrongCount(wrongCount);
        result.setUnattemptedCount(unattemptedCount);

        Result saved = resultRepository.save(result);
        attempt.setResult(saved);
        examAttemptRepository.save(attempt);

        // Send evaluation result email notification to student
        if (attempt.getStudent() != null && attempt.getStudent().getEmail() != null) {
            String courseName = (exam.getCourse() != null) ? exam.getCourse().getName() : "Course";
            emailService.sendResultEmail(
                    attempt.getStudent().getEmail(),
                    attempt.getStudent().getFullName(),
                    exam.getTitle(),
                    courseName,
                    finalScore,
                    totalMarks,
                    percentage,
                    passed,
                    attempt.getId()
            );
        }

        return saved;
    }

    @Override
    public StudentResultViewDto getStudentResult(Long attemptId, Long studentId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found with id: " + attemptId));

        if (!attempt.getStudent().getId().equals(studentId)) {
            throw new SecurityException("Unauthorized access to examination result.");
        }

        Result result = resultRepository.findByAttemptId(attemptId)
                .orElseGet(() -> evaluateAttempt(attempt));

        return buildStudentResultViewDto(attempt, result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResultViewDto> getStudentResultsHistory(Long studentId) {
        List<Result> results = resultRepository.findByAttemptStudentIdOrderByCreatedAtDesc(studentId);
        List<StudentResultViewDto> dtoList = new ArrayList<>();

        for (Result res : results) {
            ExamAttempt attempt = res.getAttempt();
            dtoList.add(buildStudentResultViewDto(attempt, res));
        }

        return dtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public ExamResultsSummaryDto getTeacherExamResults(Long examId, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found with id: " + examId));

        if (exam.getCourse().getTeacher() == null || !exam.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new SecurityException("You do not have permission to view results for this examination.");
        }

        List<Result> results = resultRepository.findByAttemptExamIdOrderByScoreDesc(examId);

        ExamResultsSummaryDto summary = new ExamResultsSummaryDto();
        summary.setExamId(exam.getId());
        summary.setExamTitle(exam.getTitle());
        summary.setCourseCode(exam.getCourse().getCode());
        summary.setCourseName(exam.getCourse().getName());
        summary.setTotalMarks(exam.getTotalMarks());
        summary.setPassingMarks(exam.getPassingMarks());
        summary.setTotalQuestions(exam.getQuestions() != null ? exam.getQuestions().size() : 0);

        int totalCandidates = results.size();
        summary.setTotalCandidates(totalCandidates);

        // Score distribution buckets: [0-20%, 21-40%, 41-60%, 61-80%, 81-100%]
        int b1 = 0, b2 = 0, b3 = 0, b4 = 0, b5 = 0;
        int passCount = 0;
        double sumScore = 0.0;

        List<TeacherStudentResultItemDto> studentItems = new ArrayList<>();
        int rank = 1;

        for (Result r : results) {
            sumScore += r.getScore();
            if (r.isPassed()) {
                passCount++;
            }

            double pct = r.getPercentage();
            if (pct <= 20.0) b1++;
            else if (pct <= 40.0) b2++;
            else if (pct <= 60.0) b3++;
            else if (pct <= 80.0) b4++;
            else b5++;

            TeacherStudentResultItemDto item = new TeacherStudentResultItemDto();
            item.setRank(rank++);
            item.setAttemptId(r.getAttempt().getId());
            item.setStudentId(r.getAttempt().getStudent().getId());
            item.setStudentName(r.getAttempt().getStudent().getFullName());
            item.setStudentEmail(r.getAttempt().getStudent().getEmail());
            item.setScore(r.getScore());
            item.setTotalMarks(r.getTotalMarks());
            item.setPercentage(r.getPercentage());
            item.setPassed(r.isPassed());
            item.setCorrectCount(r.getCorrectCount());
            item.setWrongCount(r.getWrongCount());
            item.setUnattemptedCount(r.getUnattemptedCount());
            item.setSubmittedAt(r.getAttempt().getSubmittedAt());

            studentItems.add(item);
        }

        summary.setDistributionCounts(Arrays.asList(b1, b2, b3, b4, b5));
        summary.setStudentResults(studentItems);

        if (totalCandidates > 0) {
            summary.setHighestScore(results.get(0).getScore());
            summary.setLowestScore(results.get(totalCandidates - 1).getScore());
            double avg = sumScore / totalCandidates;
            summary.setAverageScore(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP).doubleValue());
            double passPct = ((double) passCount / totalCandidates) * 100.0;
            summary.setPassPercentage(BigDecimal.valueOf(passPct).setScale(2, RoundingMode.HALF_UP).doubleValue());
        } else {
            summary.setHighestScore(0.0);
            summary.setLowestScore(0.0);
            summary.setAverageScore(0.0);
            summary.setPassPercentage(0.0);
        }

        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResultViewDto getTeacherStudentSubmissionDetail(Long examId, Long attemptId, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found with id: " + examId));

        if (exam.getCourse().getTeacher() == null || !exam.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new SecurityException("You do not have permission to inspect submissions for this exam.");
        }

        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found with id: " + attemptId));

        Result result = resultRepository.findByAttemptId(attemptId)
                .orElseGet(() -> evaluateAttempt(attempt));

        StudentResultViewDto dto = buildStudentResultViewDto(attempt, result);
        // Teacher is always authorized to review question answers
        dto.setAllowReview(true);
        return dto;
    }

    private StudentResultViewDto buildStudentResultViewDto(ExamAttempt attempt, Result result) {
        Exam exam = attempt.getExam();
        StudentResultViewDto dto = new StudentResultViewDto();
        dto.setAttemptId(attempt.getId());
        dto.setExamId(exam.getId());
        dto.setExamTitle(exam.getTitle());
        dto.setCourseCode(exam.getCourse().getCode());
        dto.setCourseName(exam.getCourse().getName());
        if (attempt.getStudent() != null) {
            dto.setStudentName(attempt.getStudent().getFullName());
            dto.setStudentEmail(attempt.getStudent().getEmail());
        }
        dto.setDurationMinutes(exam.getDurationMinutes());
        dto.setScore(result.getScore());
        dto.setTotalMarks(result.getTotalMarks());
        dto.setPercentage(result.getPercentage());
        dto.setPassed(result.isPassed());
        dto.setPassingMarks(exam.getPassingMarks());
        dto.setCorrectCount(result.getCorrectCount());
        dto.setWrongCount(result.getWrongCount());
        dto.setUnattemptedCount(result.getUnattemptedCount());
        dto.setTotalQuestions(exam.getQuestions().size());
        dto.setSubmittedAt(attempt.getSubmittedAt());
        dto.setAllowReview(exam.isShowAnswerKey());
        dto.setTabSwitchCount(attempt.getTabSwitchCount());

        // Build question-wise review items if allowed
        if (exam.isShowAnswerKey()) {
            List<StudentAnswer> answers = studentAnswerRepository.findByAttemptId(attempt.getId());
            Map<Long, StudentAnswer> answerMap = new HashMap<>();
            for (StudentAnswer sa : answers) {
                if (sa.getQuestion() != null) {
                    answerMap.put(sa.getQuestion().getId(), sa);
                }
            }

            Map<Long, Question> questionMap = exam.getQuestions().stream()
                    .collect(Collectors.toMap(Question::getId, q -> q));

            List<Long> orderedIds = new ArrayList<>();
            if (attempt.getQuestionOrder() != null && !attempt.getQuestionOrder().isBlank()) {
                orderedIds = Arrays.stream(attempt.getQuestionOrder().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            } else {
                orderedIds = exam.getQuestions().stream().map(Question::getId).collect(Collectors.toList());
            }

            List<QuestionReviewDto> reviewList = new ArrayList<>();
            int idx = 1;

            for (Long qId : orderedIds) {
                Question q = questionMap.get(qId);
                if (q == null) continue;

                QuestionReviewDto qDto = new QuestionReviewDto();
                qDto.setIndex(idx++);
                qDto.setQuestionId(q.getId());
                qDto.setQuestionText(q.getText());
                qDto.setType(q.getType());
                qDto.setQuestionMarks(q.getMarks() != null ? q.getMarks() : 1);

                // Determine correct answer text
                String correctText = q.getOptions().stream()
                        .filter(QuestionOption::isCorrect)
                        .map(QuestionOption::getOptionText)
                        .findFirst()
                        .orElse("N/A");
                qDto.setCorrectAnswerText(correctText);

                StudentAnswer sa = answerMap.get(qId);
                if (sa == null || sa.getSelectedOption() == null) {
                    qDto.setAttempted(false);
                    qDto.setCorrect(false);
                    qDto.setStudentAnswerText("Not Attempted");
                    qDto.setMarksAwarded(0.0);
                    qDto.setStatusBadgeClass("bg-light text-secondary border");
                    qDto.setStatusLabel("Not Attempted");
                } else {
                    qDto.setAttempted(true);
                    qDto.setStudentAnswerText(sa.getSelectedOption().getOptionText());

                    if (sa.getSelectedOption().isCorrect()) {
                        qDto.setCorrect(true);
                        qDto.setMarksAwarded((double) (q.getMarks() != null ? q.getMarks() : 1));
                        qDto.setStatusBadgeClass("bg-success text-white");
                        qDto.setStatusLabel("Correct");
                    } else {
                        qDto.setCorrect(false);
                        double penalty = exam.isNegativeMarking()
                                ? ((exam.getNegativeMarkValue() != null) ? exam.getNegativeMarkValue() : 0.25)
                                : 0.0;
                        qDto.setMarksAwarded(-penalty);
                        qDto.setStatusBadgeClass("bg-danger text-white");
                        qDto.setStatusLabel("Incorrect");
                    }
                }

                reviewList.add(qDto);
            }

            dto.setReviewList(reviewList);
        }

        return dto;
    }
}
