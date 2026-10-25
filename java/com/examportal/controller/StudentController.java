package com.examportal.controller;

import com.examportal.dto.AutoSaveResponseDto;
import com.examportal.dto.SaveAnswerDto;
import com.examportal.dto.StudentExamItemDto;
import com.examportal.dto.TakeExamQuestionDto;
import com.examportal.model.Exam;
import com.examportal.model.ExamAttempt;
import com.examportal.model.User;
import com.examportal.repository.UserRepository;
import com.examportal.service.ExamAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

/**
 * Controller managing student portal flows: exam catalogs, pre-exam instructions,
 * active exam taking sessions, AJAX answer auto-saving, and final submissions.
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    private final UserRepository userRepository;
    private final ExamAttemptService examAttemptService;
    private final com.examportal.service.EvaluationService evaluationService;
    private final com.examportal.service.PdfExportService pdfExportService;

    public StudentController(UserRepository userRepository, 
                             ExamAttemptService examAttemptService,
                             com.examportal.service.EvaluationService evaluationService,
                             com.examportal.service.PdfExportService pdfExportService) {
        this.userRepository = userRepository;
        this.examAttemptService = examAttemptService;
        this.evaluationService = evaluationService;
        this.pdfExportService = pdfExportService;
    }

    /**
     * Student dashboard displaying enrolled course summary and active/upcoming exams.
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User student = getAuthenticatedStudent(authentication);

        List<StudentExamItemDto> studentExams = (student != null)
                ? examAttemptService.getStudentExamsView(student.getId())
                : Collections.emptyList();

        model.addAttribute("user", student);
        model.addAttribute("enrolledCourses", student != null ? student.getEnrolledCourses() : Collections.emptySet());
        model.addAttribute("studentExams", studentExams);
        model.addAttribute("title", "Student Dashboard");
        return "student/dashboard";
    }

    /**
     * View list of available upcoming, ongoing, and completed exams for enrolled courses.
     */
    @GetMapping("/exams")
    public String availableExams(Authentication authentication, Model model) {
        User student = getAuthenticatedStudent(authentication);

        List<StudentExamItemDto> studentExams = (student != null)
                ? examAttemptService.getStudentExamsView(student.getId())
                : Collections.emptyList();

        model.addAttribute("user", student);
        model.addAttribute("enrolledCourses", student != null ? student.getEnrolledCourses() : Collections.emptySet());
        model.addAttribute("studentExams", studentExams);
        model.addAttribute("title", "Available Exams");
        return "student/exams";
    }

    /**
     * Show pre-examination instructions screen.
     */
    @GetMapping("/exams/{id}/instructions")
    public String showInstructions(@PathVariable Long id,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        User student = getAuthenticatedStudent(authentication);
        try {
            Exam exam = examAttemptService.getExamForInstructions(id, student.getId());
            model.addAttribute("exam", exam);
            model.addAttribute("user", student);
            model.addAttribute("title", "Exam Instructions - " + exam.getTitle());
            return "student/instructions";
        } catch (IllegalStateException | SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/exams";
        }
    }

    /**
     * Start a new attempt or resume an ongoing attempt.
     */
    @PostMapping("/exams/{id}/start")
    public String startExam(@PathVariable Long id,
                            @RequestParam(value = "acknowledged", defaultValue = "false") boolean acknowledged,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        User student = getAuthenticatedStudent(authentication);

        if (!acknowledged) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must acknowledge and accept the examination rules before beginning.");
            return "redirect:/student/exams/" + id + "/instructions";
        }

        try {
            ExamAttempt attempt = examAttemptService.startOrResumeAttempt(id, student.getId());
            return "redirect:/student/attempts/" + attempt.getId() + "/take";
        } catch (IllegalStateException | SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/exams";
        }
    }

    /**
     * Convenience route to resume an in-progress exam by examId.
     */
    @GetMapping("/exams/{id}/take")
    public String resumeExamByExamId(@PathVariable Long id,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        User student = getAuthenticatedStudent(authentication);
        try {
            ExamAttempt attempt = examAttemptService.startOrResumeAttempt(id, student.getId());
            return "redirect:/student/attempts/" + attempt.getId() + "/take";
        } catch (IllegalStateException | SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/exams";
        }
    }

    /**
     * Main exam taking screen.
     */
    @GetMapping("/attempts/{attemptId}/take")
    public String takeExam(@PathVariable Long attemptId,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        User student = getAuthenticatedStudent(authentication);

        try {
            ExamAttempt attempt = examAttemptService.getAttemptForTaking(attemptId, student.getId());
            long remainingSeconds = examAttemptService.calculateRemainingSeconds(attempt);
            List<TakeExamQuestionDto> questions = examAttemptService.buildTakeExamQuestions(attempt);

            // Compute initial summary counts
            int answeredCount = 0;
            int reviewCount = 0;
            int unansweredCount = 0;

            for (TakeExamQuestionDto q : questions) {
                if (q.isAnswered()) answeredCount++;
                else unansweredCount++;
                if (q.isMarkedForReview()) reviewCount++;
            }

            model.addAttribute("attempt", attempt);
            model.addAttribute("exam", attempt.getExam());
            model.addAttribute("remainingSeconds", remainingSeconds);
            model.addAttribute("questions", questions);
            model.addAttribute("totalQuestions", questions.size());
            model.addAttribute("initialAnsweredCount", answeredCount);
            model.addAttribute("initialUnansweredCount", unansweredCount);
            model.addAttribute("initialReviewCount", reviewCount);
            model.addAttribute("user", student);
            model.addAttribute("title", "Exam: " + attempt.getExam().getTitle());
            return "student/take-exam";
        } catch (IllegalStateException | SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/exams";
        }
    }

    /**
     * AJAX endpoint to autosave or clear question response.
     */
    @PostMapping("/attempts/{attemptId}/save-answer")
    @ResponseBody
    public ResponseEntity<AutoSaveResponseDto> saveAnswer(@PathVariable Long attemptId,
                                                          @RequestBody SaveAnswerDto dto,
                                                          Authentication authentication) {
        User student = getAuthenticatedStudent(authentication);
        AutoSaveResponseDto response = examAttemptService.saveAnswer(attemptId, student.getId(), dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Record a tab-switching warning event sent by student browser.
     */
    @PostMapping("/attempts/{attemptId}/tab-switch")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> recordTabSwitch(
            @PathVariable Long attemptId,
            Authentication authentication) {
        User student = getAuthenticatedStudent(authentication);
        int totalSwitches = examAttemptService.recordTabSwitch(attemptId, student.getId());
        return ResponseEntity.ok(java.util.Map.of("success", true, "tabSwitchCount", totalSwitches));
    }

    /**
     * Final submission endpoint.
     */
    @PostMapping("/attempts/{attemptId}/submit")
    public String submitExam(@PathVariable Long attemptId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User student = getAuthenticatedStudent(authentication);
        try {
            examAttemptService.submitAttempt(attemptId, student.getId());
            return "redirect:/student/attempts/" + attemptId + "/submitted";
        } catch (IllegalStateException | SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/exams";
        }
    }

    /**
     * Exam submission confirmation screen.
     */
    @GetMapping("/attempts/{attemptId}/submitted")
    public String showSubmittedScreen(@PathVariable Long attemptId,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        User student = getAuthenticatedStudent(authentication);
        try {
            ExamAttempt attempt = examAttemptService.getSubmittedAttemptSummary(attemptId, student.getId());
            model.addAttribute("attempt", attempt);
            model.addAttribute("exam", attempt.getExam());
            model.addAttribute("user", student);
            model.addAttribute("title", "Exam Submitted");
            return "student/submitted";
        } catch (IllegalStateException | SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/exams";
        }
    }

    /**
     * View history of all completed examination results for the student.
     */
    @GetMapping("/results")
    public String viewResultsHistory(Authentication authentication, Model model) {
        User student = getAuthenticatedStudent(authentication);
        List<com.examportal.dto.StudentResultViewDto> resultsHistory = 
                evaluationService.getStudentResultsHistory(student.getId());

        model.addAttribute("resultsHistory", resultsHistory);
        model.addAttribute("user", student);
        model.addAttribute("title", "My Examination Results");
        return "student/results";
    }

    /**
     * View detailed score card and question-by-question review for an examination attempt.
     */
    @GetMapping("/results/{attemptId}")
    public String viewResultDetail(@PathVariable Long attemptId,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        User student = getAuthenticatedStudent(authentication);
        try {
            com.examportal.dto.StudentResultViewDto resultDto = 
                    evaluationService.getStudentResult(attemptId, student.getId());

            model.addAttribute("result", resultDto);
            model.addAttribute("user", student);
            model.addAttribute("title", "Result - " + resultDto.getExamTitle());
            return "student/result_detail";
        } catch (IllegalStateException | SecurityException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/results";
        }
    }

    /**
     * Download official examination scorecard as PDF.
     */
    @GetMapping("/results/{attemptId}/pdf")
    public org.springframework.http.ResponseEntity<byte[]> downloadResultPdf(@PathVariable Long attemptId,
                                                                              Authentication authentication) {
        User student = getAuthenticatedStudent(authentication);
        byte[] pdfBytes = pdfExportService.generateStudentResultPdf(attemptId, student.getId());
        String filename = "Result_Attempt_" + attemptId + ".pdf";

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    private User getAuthenticatedStudent(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
    }
}
