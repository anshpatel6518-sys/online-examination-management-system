package com.examportal.service.impl;

import com.examportal.dto.AutoSaveResponseDto;
import com.examportal.dto.SaveAnswerDto;
import com.examportal.dto.StudentExamItemDto;
import com.examportal.dto.TakeExamQuestionDto;
import com.examportal.model.AttemptStatus;
import com.examportal.model.Course;
import com.examportal.model.Exam;
import com.examportal.model.ExamAttempt;
import com.examportal.model.Question;
import com.examportal.model.QuestionOption;
import com.examportal.model.StudentAnswer;
import com.examportal.model.User;
import com.examportal.repository.CourseRepository;
import com.examportal.repository.ExamAttemptRepository;
import com.examportal.repository.ExamRepository;
import com.examportal.repository.QuestionOptionRepository;
import com.examportal.repository.QuestionRepository;
import com.examportal.repository.StudentAnswerRepository;
import com.examportal.repository.UserRepository;
import com.examportal.service.EvaluationService;
import com.examportal.service.ExamAttemptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for managing student exam attempts, question sequencing, and autosave.
 */
@Service
@Transactional
public class ExamAttemptServiceImpl implements ExamAttemptService {

    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final UserRepository userRepository;
    private final EvaluationService evaluationService;

    public ExamAttemptServiceImpl(ExamRepository examRepository,
                                  ExamAttemptRepository examAttemptRepository,
                                  StudentAnswerRepository studentAnswerRepository,
                                  QuestionRepository questionRepository,
                                  QuestionOptionRepository questionOptionRepository,
                                  UserRepository userRepository,
                                  EvaluationService evaluationService) {
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.userRepository = userRepository;
        this.evaluationService = evaluationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentExamItemDto> getStudentExamsView(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalStateException("Student not found."));

        List<Long> enrolledCourseIds = student.getEnrolledCourses().stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        if (enrolledCourseIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Exam> publishedExams = examRepository.findByCourseIdInAndPublishedTrue(enrolledCourseIds);
        List<StudentExamItemDto> resultList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Exam exam : publishedExams) {
            StudentExamItemDto dto = new StudentExamItemDto();
            dto.setExamId(exam.getId());
            dto.setTitle(exam.getTitle());
            dto.setCourseCode(exam.getCourse().getCode());
            dto.setCourseName(exam.getCourse().getName());
            dto.setDurationMinutes(exam.getDurationMinutes());
            dto.setTotalMarks(exam.getTotalMarks());
            dto.setPassingMarks(exam.getPassingMarks());
            dto.setStartTime(exam.getStartTime());
            dto.setEndTime(exam.getEndTime());
            dto.setNegativeMarking(exam.isNegativeMarking());
            dto.setNegativeMarkValue(exam.getNegativeMarkValue());

            // Check student's attempts
            List<ExamAttempt> attempts = examAttemptRepository.findByStudentIdAndExamId(studentId, exam.getId());
            boolean hasAttempted = false;
            boolean isInProgress = false;
            Long inProgressAttemptId = null;

            for (ExamAttempt att : attempts) {
                if (att.getStatus() == AttemptStatus.SUBMITTED) {
                    hasAttempted = true;
                    break;
                } else if (att.getStatus() == AttemptStatus.IN_PROGRESS) {
                    long rem = calculateRemainingSeconds(att);
                    if (rem <= 0) {
                        // Expired in-progress attempt - mark as submitted
                        att.setStatus(AttemptStatus.SUBMITTED);
                        att.setSubmittedAt(att.getStartedAt().plusMinutes(exam.getDurationMinutes()));
                        examAttemptRepository.save(att);
                        hasAttempted = true;
                        break;
                    } else {
                        isInProgress = true;
                        inProgressAttemptId = att.getId();
                    }
                }
            }

            dto.setHasAttempted(hasAttempted);
            dto.setCanResume(isInProgress);
            dto.setAttemptId(inProgressAttemptId);

            boolean isStarted = (exam.getStartTime() == null || !now.isBefore(exam.getStartTime()));
            boolean isEnded = (exam.getEndTime() != null && now.isAfter(exam.getEndTime()));

            if (hasAttempted) {
                dto.setStatus("Completed");
                dto.setButtonLabel("Completed");
                dto.setButtonClass("btn-secondary");
                dto.setButtonDisabled(true);
            } else if (isInProgress) {
                dto.setStatus("Ongoing");
                dto.setButtonLabel("Resume Exam");
                dto.setButtonClass("btn-dark");
                dto.setButtonDisabled(false);
                dto.setActionUrl("/student/exams/" + exam.getId() + "/take");
            } else if (!isStarted) {
                dto.setStatus("Upcoming");
                dto.setButtonLabel("Upcoming");
                dto.setButtonClass("btn-outline-secondary");
                dto.setButtonDisabled(true);
            } else if (isEnded) {
                dto.setStatus("Completed");
                dto.setButtonLabel("Closed");
                dto.setButtonClass("btn-outline-secondary");
                dto.setButtonDisabled(true);
            } else {
                // Window is open and not yet attempted
                dto.setStatus("Ongoing");
                dto.setCanStart(true);
                dto.setButtonLabel("Start Exam");
                dto.setButtonClass("btn-dark");
                dto.setButtonDisabled(false);
                dto.setActionUrl("/student/exams/" + exam.getId() + "/instructions");
            }

            resultList.add(dto);
        }

        return resultList;
    }

    @Override
    @Transactional(readOnly = true)
    public Exam getExamForInstructions(Long examId, Long studentId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found with id: " + examId));

        if (!exam.isPublished()) {
            throw new SecurityException("This examination is not currently published.");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalStateException("Student not found."));

        boolean enrolled = student.getEnrolledCourses().stream()
                .anyMatch(c -> c.getId().equals(exam.getCourse().getId()));
        if (!enrolled) {
            throw new SecurityException("You are not enrolled in the course associated with this exam.");
        }

        boolean submitted = examAttemptRepository.existsByStudentIdAndExamIdAndStatus(studentId, examId, AttemptStatus.SUBMITTED);
        if (submitted) {
            throw new IllegalStateException("You have already submitted an attempt for this examination.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
            throw new IllegalStateException("This examination window has not opened yet.");
        }
        if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
            throw new IllegalStateException("This examination schedule window has expired.");
        }

        return exam;
    }

    @Override
    public ExamAttempt startOrResumeAttempt(Long examId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalStateException("Student not found."));

        // Check if an attempt is already IN_PROGRESS
        Optional<ExamAttempt> existingInProgress = examAttemptRepository
                .findByStudentIdAndExamIdAndStatus(studentId, examId, AttemptStatus.IN_PROGRESS);

        if (existingInProgress.isPresent()) {
            ExamAttempt attempt = existingInProgress.get();
            long remaining = calculateRemainingSeconds(attempt);
            if (remaining <= 0) {
                attempt.setStatus(AttemptStatus.SUBMITTED);
                attempt.setSubmittedAt(attempt.getStartedAt().plusMinutes(attempt.getExam().getDurationMinutes()));
                examAttemptRepository.save(attempt);
                throw new IllegalStateException("Exam time has expired.");
            }
            return attempt;
        }

        // Validate before creating new attempt
        Exam exam = getExamForInstructions(examId, studentId);

        List<Question> questions = new ArrayList<>(exam.getQuestions());
        if (questions.isEmpty()) {
            throw new IllegalStateException("This exam does not have any questions assigned.");
        }

        // Shuffle questions if enabled
        if (exam.isShuffleQuestions()) {
            Collections.shuffle(questions);
        }

        // Store question sequence as comma-separated IDs
        String questionOrder = questions.stream()
                .map(q -> String.valueOf(q.getId()))
                .collect(Collectors.joining(","));

        ExamAttempt newAttempt = new ExamAttempt();
        newAttempt.setStudent(student);
        newAttempt.setExam(exam);
        newAttempt.setStartedAt(LocalDateTime.now());
        newAttempt.setStatus(AttemptStatus.IN_PROGRESS);
        newAttempt.setQuestionOrder(questionOrder);

        ExamAttempt savedAttempt = examAttemptRepository.save(newAttempt);

        // Pre-create empty StudentAnswer records to track state and answers deterministically
        for (Question q : questions) {
            StudentAnswer sa = new StudentAnswer(null, savedAttempt, q, null, false);
            studentAnswerRepository.save(sa);
        }

        return savedAttempt;
    }

    @Override
    public ExamAttempt getAttemptForTaking(Long attemptId, Long studentId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Exam attempt not found."));

        if (!attempt.getStudent().getId().equals(studentId)) {
            throw new SecurityException("You do not have permission to access this examination attempt.");
        }

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            throw new IllegalStateException("This examination attempt has already been submitted.");
        }

        long remaining = calculateRemainingSeconds(attempt);
        if (remaining <= 0) {
            attempt.setStatus(AttemptStatus.SUBMITTED);
            attempt.setSubmittedAt(attempt.getStartedAt().plusMinutes(attempt.getExam().getDurationMinutes()));
            examAttemptRepository.save(attempt);
            throw new IllegalStateException("Exam time has expired.");
        }

        return attempt;
    }

    @Override
    @Transactional(readOnly = true)
    public long calculateRemainingSeconds(ExamAttempt attempt) {
        LocalDateTime startedAt = attempt.getStartedAt();
        int durationMinutes = attempt.getExam().getDurationMinutes();
        LocalDateTime attemptDeadline = startedAt.plusMinutes(durationMinutes);
        LocalDateTime now = LocalDateTime.now();

        long remaining = Duration.between(now, attemptDeadline).getSeconds();

        // Also constrain by exam's absolute window closing time if specified
        if (attempt.getExam().getEndTime() != null) {
            long windowRemaining = Duration.between(now, attempt.getExam().getEndTime()).getSeconds();
            remaining = Math.min(remaining, windowRemaining);
        }

        return Math.max(0, remaining);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TakeExamQuestionDto> buildTakeExamQuestions(ExamAttempt attempt) {
        List<Question> questions = new ArrayList<>(attempt.getExam().getQuestions());
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // Determine ordering from saved questionOrder
        List<Long> orderedIds = new ArrayList<>();
        if (attempt.getQuestionOrder() != null && !attempt.getQuestionOrder().isBlank()) {
            orderedIds = Arrays.stream(attempt.getQuestionOrder().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } else {
            orderedIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        }

        // Map existing student answers for this attempt
        List<StudentAnswer> answers = studentAnswerRepository.findByAttemptId(attempt.getId());
        Map<Long, StudentAnswer> answerMap = new HashMap<>();
        for (StudentAnswer sa : answers) {
            if (sa.getQuestion() != null) {
                answerMap.put(sa.getQuestion().getId(), sa);
            }
        }

        List<TakeExamQuestionDto> dtoList = new ArrayList<>();
        int index = 1;

        for (Long qId : orderedIds) {
            Question q = questionMap.get(qId);
            if (q == null) continue;

            TakeExamQuestionDto dto = new TakeExamQuestionDto();
            dto.setIndex(index++);
            dto.setQuestionId(q.getId());
            dto.setText(q.getText());
            dto.setType(q.getType());
            dto.setMarks(q.getMarks() != null ? q.getMarks() : 1);
            dto.setOptions(q.getOptions());

            StudentAnswer sa = answerMap.get(qId);
            if (sa != null) {
                dto.setSelectedOptionId(sa.getSelectedOption() != null ? sa.getSelectedOption().getId() : null);
                dto.setMarkedForReview(sa.isMarkedForReview());
                dto.setAnswered(sa.getSelectedOption() != null);
            }

            dtoList.add(dto);
        }

        return dtoList;
    }

    @Override
    public AutoSaveResponseDto saveAnswer(Long attemptId, Long studentId, SaveAnswerDto dto) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found."));

        if (!attempt.getStudent().getId().equals(studentId)) {
            return new AutoSaveResponseDto(false, 0, false, "Unauthorized attempt access.");
        }

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            return new AutoSaveResponseDto(false, 0, true, "Exam has already been submitted.");
        }

        // Server-side time validation
        long remaining = calculateRemainingSeconds(attempt);
        if (remaining <= 0) {
            attempt.setStatus(AttemptStatus.SUBMITTED);
            attempt.setSubmittedAt(attempt.getStartedAt().plusMinutes(attempt.getExam().getDurationMinutes()));
            examAttemptRepository.save(attempt);
            return new AutoSaveResponseDto(false, 0, true, "Exam time has expired. Exam submitted automatically.");
        }

        StudentAnswer studentAnswer = studentAnswerRepository
                .findByAttemptIdAndQuestionId(attemptId, dto.getQuestionId())
                .orElseGet(() -> {
                    Question q = questionRepository.findById(dto.getQuestionId())
                            .orElseThrow(() -> new IllegalArgumentException("Question not found."));
                    return new StudentAnswer(null, attempt, q, null, false);
                });

        if (dto.getSelectedOptionId() != null) {
            QuestionOption option = questionOptionRepository.findById(dto.getSelectedOptionId())
                    .orElse(null);
            studentAnswer.setSelectedOption(option);
        } else {
            // Clearing answer
            studentAnswer.setSelectedOption(null);
        }

        if (dto.getMarkedForReview() != null) {
            studentAnswer.setMarkedForReview(dto.getMarkedForReview());
        }

        studentAnswerRepository.save(studentAnswer);

        // Calculate counts
        List<StudentAnswer> allAnswers = studentAnswerRepository.findByAttemptId(attemptId);
        int answeredCount = 0;
        int reviewCount = 0;
        int unansweredCount = 0;

        for (StudentAnswer sa : allAnswers) {
            if (sa.getSelectedOption() != null) {
                answeredCount++;
            } else {
                unansweredCount++;
            }
            if (sa.isMarkedForReview()) {
                reviewCount++;
            }
        }

        return new AutoSaveResponseDto(true, remaining, false, "Answer saved",
                answeredCount, unansweredCount, reviewCount);
    }

    @Override
    public void submitAttempt(Long attemptId, Long studentId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found."));

        if (!attempt.getStudent().getId().equals(studentId)) {
            throw new SecurityException("Unauthorized access to exam attempt.");
        }

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            evaluationService.evaluateAttempt(attempt);
            return;
        }

        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());
        ExamAttempt savedAttempt = examAttemptRepository.save(attempt);
        evaluationService.evaluateAttempt(savedAttempt);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamAttempt getSubmittedAttemptSummary(Long attemptId, Long studentId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found."));

        if (!attempt.getStudent().getId().equals(studentId)) {
            throw new SecurityException("Unauthorized attempt access.");
        }

        return attempt;
    }

    @Override
    @Transactional
    public int recordTabSwitch(Long attemptId, Long studentId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found."));

        if (!attempt.getStudent().getId().equals(studentId)) {
            throw new SecurityException("Unauthorized attempt access.");
        }

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            return attempt.getTabSwitchCount();
        }

        attempt.setTabSwitchCount(attempt.getTabSwitchCount() + 1);
        examAttemptRepository.save(attempt);
        return attempt.getTabSwitchCount();
    }
}
