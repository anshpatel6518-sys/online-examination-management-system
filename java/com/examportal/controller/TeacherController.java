package com.examportal.controller;

import com.examportal.dto.BulkUploadResultDto;
import com.examportal.dto.ExamDto;
import com.examportal.dto.QuestionDto;
import com.examportal.model.Course;
import com.examportal.model.Difficulty;
import com.examportal.model.Exam;
import com.examportal.model.Question;
import com.examportal.model.QuestionType;
import com.examportal.model.User;
import com.examportal.repository.CourseRepository;
import com.examportal.repository.QuestionRepository;
import com.examportal.repository.UserRepository;
import com.examportal.service.ExamService;
import com.examportal.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller handling teacher dashboard, course question bank, and exam lifecycle management.
 */
@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    private final QuestionService questionService;
    private final ExamService examService;
    private final com.examportal.service.EvaluationService evaluationService;
    private final com.examportal.service.ExcelExportService excelExportService;

    public TeacherController(UserRepository userRepository,
                             CourseRepository courseRepository,
                             QuestionRepository questionRepository,
                             QuestionService questionService,
                             ExamService examService,
                             com.examportal.service.EvaluationService evaluationService,
                             com.examportal.service.ExcelExportService excelExportService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.questionRepository = questionRepository;
        this.questionService = questionService;
        this.examService = examService;
        this.evaluationService = evaluationService;
        this.excelExportService = excelExportService;
    }

    /**
     * Teacher dashboard displaying assigned courses and statistics.
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<Course> assignedCourses = courseRepository.findByTeacherId(teacher.getId());
        long questionCount = questionService.getQuestionCountByTeacher(teacher.getId());
        long examCount = examService.getExamCountByTeacher(teacher.getId());

        model.addAttribute("user", teacher);
        model.addAttribute("courses", assignedCourses);
        model.addAttribute("totalQuestions", questionCount);
        model.addAttribute("totalExams", examCount);
        model.addAttribute("title", "Teacher Dashboard");
        return "teacher/dashboard";
    }

    // =========================================================================
    // EXAM MANAGEMENT ROUTES
    // =========================================================================

    /**
     * List all exams configured by the teacher.
     */
    @GetMapping("/exams")
    public String listExams(Authentication authentication, Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<Exam> exams = examService.getExamsByTeacher(teacher.getId());

        model.addAttribute("exams", exams);
        model.addAttribute("title", "My Exams");
        return "teacher/exams";
    }

    /**
     * Show form to create an exam.
     */
    @GetMapping("/exams/new")
    public String showCreateExamForm(Authentication authentication,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<Course> assignedCourses = courseRepository.findByTeacherId(teacher.getId());

        if (assignedCourses.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must be assigned to at least one course by an administrator before scheduling exams.");
            return "redirect:/teacher/exams";
        }

        ExamDto dto = new ExamDto();
        dto.setCourseId(assignedCourses.get(0).getId());

        model.addAttribute("exam", dto);
        model.addAttribute("courses", assignedCourses);
        model.addAttribute("isEdit", false);
        model.addAttribute("title", "Create Exam");
        return "teacher/exam_form";
    }

    /**
     * Process creation of an exam.
     */
    @PostMapping("/exams/new")
    public String createExam(@Valid @ModelAttribute("exam") ExamDto examDto,
                             BindingResult bindingResult,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<Course> assignedCourses = courseRepository.findByTeacherId(teacher.getId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", assignedCourses);
            model.addAttribute("isEdit", false);
            model.addAttribute("title", "Create Exam");
            return "teacher/exam_form";
        }

        try {
            Long createdExamId = examService.createExam(examDto, teacher.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Exam created. Now select questions from the Question Bank.");
            return "redirect:/teacher/exams/" + createdExamId + "/questions";
        } catch (IllegalArgumentException | SecurityException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("courses", assignedCourses);
            model.addAttribute("isEdit", false);
            model.addAttribute("title", "Create Exam");
            return "teacher/exam_form";
        }
    }

    /**
     * Show form to edit exam settings.
     */
    @GetMapping("/exams/edit/{id}")
    public String showEditExamForm(@PathVariable Long id,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        try {
            ExamDto dto = examService.getExamDtoByIdAndTeacher(id, teacher.getId());
            List<Course> assignedCourses = courseRepository.findByTeacherId(teacher.getId());

            model.addAttribute("exam", dto);
            model.addAttribute("courses", assignedCourses);
            model.addAttribute("isEdit", true);
            model.addAttribute("hasAttempts", examService.hasAttempts(id));
            model.addAttribute("title", "Edit Exam");
            return "teacher/exam_form";
        } catch (SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teacher/exams";
        }
    }

    /**
     * Process editing of exam settings.
     */
    @PostMapping("/exams/edit/{id}")
    public String updateExam(@PathVariable Long id,
                             @Valid @ModelAttribute("exam") ExamDto examDto,
                             BindingResult bindingResult,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<Course> assignedCourses = courseRepository.findByTeacherId(teacher.getId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", assignedCourses);
            model.addAttribute("isEdit", true);
            model.addAttribute("hasAttempts", examService.hasAttempts(id));
            model.addAttribute("title", "Edit Exam");
            return "teacher/exam_form";
        }

        try {
            examService.updateExam(id, examDto, teacher.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Exam configuration updated successfully.");
            return "redirect:/teacher/exams";
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("courses", assignedCourses);
            model.addAttribute("isEdit", true);
            model.addAttribute("hasAttempts", examService.hasAttempts(id));
            model.addAttribute("title", "Edit Exam");
            return "teacher/exam_form";
        }
    }

    /**
     * Manage questions checklist for an exam.
     */
    @GetMapping("/exams/{id}/questions")
    public String showExamQuestionsChecklist(@PathVariable Long id,
                                             Authentication authentication,
                                             RedirectAttributes redirectAttributes,
                                             Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        try {
            Exam exam = examService.getExamByIdAndTeacher(id, teacher.getId());
            List<Question> bankQuestions = questionRepository.findByCourseId(exam.getCourse().getId());
            boolean hasAttempts = examService.hasAttempts(id);

            List<Long> linkedQuestionIds = new ArrayList<>();
            for (Question q : exam.getQuestions()) {
                linkedQuestionIds.add(q.getId());
            }

            model.addAttribute("exam", exam);
            model.addAttribute("bankQuestions", bankQuestions);
            model.addAttribute("linkedQuestionIds", linkedQuestionIds);
            model.addAttribute("hasAttempts", hasAttempts);
            model.addAttribute("title", "Select Exam Questions");
            return "teacher/exam_questions";
        } catch (SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teacher/exams";
        }
    }

    /**
     * Save question checklist selections for an exam.
     */
    @PostMapping("/exams/{id}/questions")
    public String updateExamQuestions(@PathVariable Long id,
                                      @RequestParam(value = "questionIds", required = false) List<Long> questionIds,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        User teacher = getAuthenticatedTeacher(authentication);
        try {
            examService.updateExamQuestions(id, questionIds, teacher.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Exam question selections and total marks updated successfully.");
        } catch (IllegalStateException | SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/teacher/exams/" + id + "/questions";
    }

    /**
     * Toggle exam published status.
     */
    @PostMapping("/exams/{id}/toggle-publish")
    public String togglePublishExam(@PathVariable Long id,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        User teacher = getAuthenticatedTeacher(authentication);
        try {
            examService.togglePublish(id, teacher.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Exam publish status updated successfully.");
        } catch (IllegalStateException | SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/teacher/exams";
    }

    /**
     * Delete an exam.
     */
    @PostMapping("/exams/{id}/delete")
    public String deleteExam(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User teacher = getAuthenticatedTeacher(authentication);
        try {
            examService.deleteExam(id, teacher.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Exam deleted successfully.");
        } catch (IllegalStateException | SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/teacher/exams";
    }

    // =========================================================================
    // QUESTION BANK ROUTES
    // =========================================================================

    /**
     * Question Bank listing with course and difficulty filters.
     */
    @GetMapping("/questions")
    public String listQuestions(@RequestParam(required = false) Long courseId,
                                @RequestParam(required = false) Difficulty difficulty,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Authentication authentication,
                                Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<Course> assignedCourses = courseRepository.findByTeacherId(teacher.getId());

        Page<Question> questionsPage = questionService.getQuestionsByTeacher(teacher.getId(), courseId, difficulty, page, size);

        model.addAttribute("questionsPage", questionsPage);
        model.addAttribute("questions", questionsPage.getContent());
        model.addAttribute("courses", assignedCourses);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedDifficulty", difficulty);
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questionsPage.getTotalPages());
        model.addAttribute("totalItems", questionsPage.getTotalElements());
        model.addAttribute("title", "Question Bank");
        return "teacher/questions";
    }

    /**
     * Form to create a new question.
     */
    @GetMapping("/questions/new")
    public String showCreateQuestionForm(Authentication authentication,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<Course> assignedCourses = courseRepository.findByTeacherId(teacher.getId());

        if (assignedCourses.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must be assigned to at least one course by an administrator before adding questions.");
            return "redirect:/teacher/questions";
        }

        QuestionDto dto = new QuestionDto();
        dto.setCourseId(assignedCourses.get(0).getId());

        model.addAttribute("question", dto);
        model.addAttribute("courses", assignedCourses);
        model.addAttribute("types", QuestionType.values());
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("isEdit", false);
        model.addAttribute("title", "Add Question");
        return "teacher/question_form";
    }

    /**
     * Process creation of a new question.
     */
    @PostMapping("/questions/new")
    public String createQuestion(@Valid @ModelAttribute("question") QuestionDto questionDto,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<Course> assignedCourses = courseRepository.findByTeacherId(teacher.getId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", assignedCourses);
            model.addAttribute("types", QuestionType.values());
            model.addAttribute("difficulties", Difficulty.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("title", "Add Question");
            return "teacher/question_form";
        }

        try {
            questionService.createQuestion(questionDto, teacher.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Question added to Question Bank successfully.");
            return "redirect:/teacher/questions";
        } catch (IllegalArgumentException | SecurityException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("courses", assignedCourses);
            model.addAttribute("types", QuestionType.values());
            model.addAttribute("difficulties", Difficulty.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("title", "Add Question");
            return "teacher/question_form";
        }
    }

    /**
     * Form to edit an existing question.
     */
    @GetMapping("/questions/edit/{id}")
    public String showEditQuestionForm(@PathVariable Long id,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        try {
            QuestionDto dto = questionService.getQuestionDtoByIdAndTeacher(id, teacher.getId());
            List<Course> assignedCourses = courseRepository.findByTeacherId(teacher.getId());

            model.addAttribute("question", dto);
            model.addAttribute("courses", assignedCourses);
            model.addAttribute("types", QuestionType.values());
            model.addAttribute("difficulties", Difficulty.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("title", "Edit Question");
            return "teacher/question_form";
        } catch (SecurityException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teacher/questions";
        }
    }

    /**
     * Process editing of an existing question.
     */
    @PostMapping("/questions/edit/{id}")
    public String updateQuestion(@PathVariable Long id,
                                 @Valid @ModelAttribute("question") QuestionDto questionDto,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<Course> assignedCourses = courseRepository.findByTeacherId(teacher.getId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", assignedCourses);
            model.addAttribute("types", QuestionType.values());
            model.addAttribute("difficulties", Difficulty.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("title", "Edit Question");
            return "teacher/question_form";
        }

        try {
            questionService.updateQuestion(id, questionDto, teacher.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Question updated successfully.");
            return "redirect:/teacher/questions";
        } catch (IllegalArgumentException | SecurityException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("courses", assignedCourses);
            model.addAttribute("types", QuestionType.values());
            model.addAttribute("difficulties", Difficulty.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("title", "Edit Question");
            return "teacher/question_form";
        }
    }

    /**
     * Delete a question from the question bank.
     */
    @PostMapping("/questions/delete/{id}")
    public String deleteQuestion(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        User teacher = getAuthenticatedTeacher(authentication);
        try {
            questionService.deleteQuestion(id, teacher.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Question deleted from Question Bank.");
        } catch (SecurityException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/teacher/questions";
    }

    /**
     * Show form for bulk question upload via CSV.
     */
    @GetMapping("/questions/upload")
    public String showBulkUploadForm(Authentication authentication,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<Course> assignedCourses = courseRepository.findByTeacherId(teacher.getId());

        if (assignedCourses.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must be assigned to at least one course by an administrator before uploading questions.");
            return "redirect:/teacher/questions";
        }

        model.addAttribute("courses", assignedCourses);
        model.addAttribute("title", "Bulk Upload Questions");
        return "teacher/upload_questions";
    }

    /**
     * Process bulk question CSV upload.
     */
    @PostMapping("/questions/upload")
    public String processBulkUpload(@RequestParam("courseId") Long courseId,
                                    @RequestParam("file") MultipartFile file,
                                    Authentication authentication,
                                    Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<Course> assignedCourses = courseRepository.findByTeacherId(teacher.getId());

        model.addAttribute("courses", assignedCourses);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("title", "Bulk Upload Questions");

        try {
            BulkUploadResultDto result = questionService.bulkUploadQuestions(file, courseId, teacher.getId());
            model.addAttribute("uploadResult", result);
        } catch (SecurityException | IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }

        return "teacher/upload_questions";
    }

    /**
     * Teacher results landing page listing all exams with evaluation summaries.
     */
    @GetMapping("/results")
    public String viewAllExamResults(Authentication authentication, Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        List<Exam> exams = examService.getExamsByTeacher(teacher.getId());

        List<com.examportal.dto.ExamResultsSummaryDto> summaries = new ArrayList<>();
        for (Exam e : exams) {
            summaries.add(evaluationService.getTeacherExamResults(e.getId(), teacher.getId()));
        }

        model.addAttribute("exams", exams);
        model.addAttribute("summaries", summaries);
        model.addAttribute("user", teacher);
        model.addAttribute("title", "Exam Results & Performance");
        return "teacher/results";
    }

    /**
     * Teacher specific exam results page with student rankings, metrics, and score distribution chart.
     */
    @GetMapping("/exams/{id}/results")
    public String viewExamResultsDetail(@PathVariable Long id,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        try {
            com.examportal.dto.ExamResultsSummaryDto summary = 
                    evaluationService.getTeacherExamResults(id, teacher.getId());

            model.addAttribute("summary", summary);
            model.addAttribute("user", teacher);
            model.addAttribute("title", "Results - " + summary.getExamTitle());
            return "teacher/exam_results";
        } catch (SecurityException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teacher/results";
        }
    }

    /**
     * Teacher detailed inspection of an individual student's submission.
     */
    @GetMapping("/exams/{id}/submissions/{attemptId}")
    public String viewStudentSubmission(@PathVariable Long id,
                                        @PathVariable Long attemptId,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        User teacher = getAuthenticatedTeacher(authentication);
        try {
            com.examportal.dto.StudentResultViewDto detail = 
                    evaluationService.getTeacherStudentSubmissionDetail(id, attemptId, teacher.getId());

            model.addAttribute("result", detail);
            model.addAttribute("examId", id);
            model.addAttribute("user", teacher);
            model.addAttribute("title", "Submission Detail - " + detail.getExamTitle());
            return "teacher/student_submission";
        } catch (SecurityException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/teacher/exams/" + id + "/results";
        }
    }

    /**
     * Export exam results as an Excel spreadsheet (.xlsx).
     */
    @GetMapping("/exams/{id}/results/excel")
    public org.springframework.http.ResponseEntity<byte[]> exportExamResultsExcel(@PathVariable Long id,
                                                                                  Authentication authentication) {
        User teacher = getAuthenticatedTeacher(authentication);
        byte[] excelBytes = excelExportService.exportExamResultsToExcel(id, teacher.getId());
        String filename = "Exam_Results_" + id + ".xlsx";

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelBytes);
    }

    private User getAuthenticatedTeacher(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
    }
}
