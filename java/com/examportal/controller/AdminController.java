package com.examportal.controller;

import com.examportal.dto.AdminStatsDto;
import com.examportal.dto.CourseDto;
import com.examportal.dto.CreateTeacherDto;
import com.examportal.model.Course;
import com.examportal.model.User;
import com.examportal.service.CourseService;
import com.examportal.service.UserService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for administrator portal actions.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;
    private final com.examportal.repository.ExamRepository examRepository;
    private final com.examportal.service.ReportService reportService;
    private final com.examportal.service.ExcelExportService excelExportService;

    public AdminController(UserService userService, 
                           CourseService courseService,
                           com.examportal.repository.ExamRepository examRepository,
                           com.examportal.service.ReportService reportService,
                           com.examportal.service.ExcelExportService excelExportService) {
        this.userService = userService;
        this.courseService = courseService;
        this.examRepository = examRepository;
        this.reportService = reportService;
        this.excelExportService = excelExportService;
    }

    /**
     * Administrator Dashboard with live database statistics and chart data.
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        AdminStatsDto stats = userService.getAdminStats();
        List<Course> courses = courseService.getAllCourses();

        List<String> courseLabels = new java.util.ArrayList<>();
        List<Long> examCounts = new java.util.ArrayList<>();
        for (Course c : courses) {
            courseLabels.add(c.getCode());
            examCounts.add(examRepository.countByCourseId(c.getId()));
        }

        model.addAttribute("stats", stats);
        model.addAttribute("courses", courses);
        model.addAttribute("courseLabels", courseLabels);
        model.addAttribute("examCounts", examCounts);
        model.addAttribute("title", "Admin Dashboard");
        return "admin/dashboard";
    }

    /**
     * List all users with search and pagination.
     */
    @GetMapping("/users")
    public String listUsers(@RequestParam(defaultValue = "") String search,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        Page<User> usersPage = userService.getUsers(search, page, size);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalItems", usersPage.getTotalElements());
        model.addAttribute("title", "User Management");
        return "admin/users";
    }

    /**
     * Show form to create a teacher account.
     */
    @GetMapping("/users/new-teacher")
    public String showCreateTeacherForm(Model model) {
        model.addAttribute("teacher", new CreateTeacherDto());
        model.addAttribute("title", "Add Teacher");
        return "admin/new_teacher";
    }

    /**
     * Process creation of a teacher account.
     */
    @PostMapping("/users/new-teacher")
    public String createTeacher(@Valid @ModelAttribute("teacher") CreateTeacherDto teacherDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (!teacherDto.getPassword().equals(teacherDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Add Teacher");
            return "admin/new_teacher";
        }

        try {
            userService.createTeacher(teacherDto);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher account for " + teacherDto.getFullName() + " created successfully.");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("email", "email.duplicate", ex.getMessage());
            model.addAttribute("title", "Add Teacher");
            return "admin/new_teacher";
        }
    }

    /**
     * Toggle user enabled/disabled status.
     */
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Delete user account.
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Show student course enrollment management form.
     */
    @GetMapping("/users/{id}/enroll")
    public String showEnrollmentForm(@PathVariable Long id, Model model) {
        com.examportal.dto.EnrollmentDto enrollmentDto = userService.getStudentEnrollmentDto(id);
        List<Course> allCourses = courseService.getAllCourses();

        model.addAttribute("enrollment", enrollmentDto);
        model.addAttribute("courses", allCourses);
        model.addAttribute("title", "Enroll Student in Courses");
        return "admin/enroll_student";
    }

    /**
     * Process student course enrollment updates.
     */
    @PostMapping("/users/{id}/enroll")
    public String updateEnrollments(@PathVariable Long id,
                                   @RequestParam(value = "courseIds", required = false) List<Long> courseIds,
                                   RedirectAttributes redirectAttributes) {
        userService.updateStudentEnrollments(id, courseIds);
        redirectAttributes.addFlashAttribute("successMessage", "Student course enrollments updated successfully.");
        return "redirect:/admin/users";
    }

    /**
     * List all courses.
     */
    @GetMapping("/courses")
    public String listCourses(Model model) {
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        model.addAttribute("title", "Course Management");
        return "admin/courses";
    }

    /**
     * Show create course form.
     */
    @GetMapping("/courses/new")
    public String showCreateCourseForm(Model model) {
        model.addAttribute("course", new CourseDto());
        model.addAttribute("teachers", userService.getAllTeachers());
        model.addAttribute("isEdit", false);
        model.addAttribute("title", "Create Course");
        return "admin/course_form";
    }

    /**
     * Process create course.
     */
    @PostMapping("/courses/new")
    public String createCourse(@Valid @ModelAttribute("course") CourseDto courseDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("teachers", userService.getAllTeachers());
            model.addAttribute("isEdit", false);
            model.addAttribute("title", "Create Course");
            return "admin/course_form";
        }

        try {
            courseService.createCourse(courseDto);
            redirectAttributes.addFlashAttribute("successMessage", "Course '" + courseDto.getName() + "' created successfully.");
            return "redirect:/admin/courses";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("code", "code.duplicate", ex.getMessage());
            model.addAttribute("teachers", userService.getAllTeachers());
            model.addAttribute("isEdit", false);
            model.addAttribute("title", "Create Course");
            return "admin/course_form";
        }
    }

    /**
     * Show edit course form.
     */
    @GetMapping("/courses/edit/{id}")
    public String showEditCourseForm(@PathVariable Long id, Model model) {
        CourseDto courseDto = courseService.getCourseDtoById(id);
        model.addAttribute("course", courseDto);
        model.addAttribute("teachers", userService.getAllTeachers());
        model.addAttribute("isEdit", true);
        model.addAttribute("title", "Edit Course");
        return "admin/course_form";
    }

    /**
     * Process edit course.
     */
    @PostMapping("/courses/edit/{id}")
    public String updateCourse(@PathVariable Long id,
                               @Valid @ModelAttribute("course") CourseDto courseDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("teachers", userService.getAllTeachers());
            model.addAttribute("isEdit", true);
            model.addAttribute("title", "Edit Course");
            return "admin/course_form";
        }

        try {
            courseService.updateCourse(id, courseDto);
            redirectAttributes.addFlashAttribute("successMessage", "Course updated successfully.");
            return "redirect:/admin/courses";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("code", "code.duplicate", ex.getMessage());
            model.addAttribute("teachers", userService.getAllTeachers());
            model.addAttribute("isEdit", true);
            model.addAttribute("title", "Edit Course");
            return "admin/course_form";
        }
    }

    /**
     * Process delete course.
     */
    @PostMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("successMessage", "Course deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/courses";
    }

    /**
     * Reports page with multi-criteria filters and statistical summaries.
     */
    @GetMapping("/reports")
    public String viewReports(@RequestParam(name = "courseId", required = false) Long courseId,
                              @RequestParam(name = "examId", required = false) Long examId,
                              @RequestParam(name = "fromDate", required = false)
                              @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                              java.time.LocalDate fromDate,
                              @RequestParam(name = "toDate", required = false)
                              @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                              java.time.LocalDate toDate,
                              @RequestParam(name = "status", required = false, defaultValue = "ALL") String status,
                              Model model) {

        com.examportal.dto.AdminReportSummaryDto report = 
                reportService.generateAdminReport(courseId, examId, fromDate, toDate, status);

        model.addAttribute("report", report);
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("exams", examRepository.findAll());
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedExamId", examId);
        model.addAttribute("selectedFromDate", fromDate);
        model.addAttribute("selectedToDate", toDate);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("title", "Examination Reports & Analytics");
        return "admin/reports";
    }

    /**
     * Export filtered examination reports to Excel (.xlsx).
     */
    @GetMapping("/reports/excel")
    public org.springframework.http.ResponseEntity<byte[]> exportReportsExcel(
            @RequestParam(name = "courseId", required = false) Long courseId,
            @RequestParam(name = "examId", required = false) Long examId,
            @RequestParam(name = "fromDate", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate fromDate,
            @RequestParam(name = "toDate", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate toDate,
            @RequestParam(name = "status", required = false, defaultValue = "ALL") String status) {

        byte[] excelBytes = excelExportService.exportAdminReportsToExcel(courseId, examId, fromDate, toDate, status);
        String filename = "Exam_Reports_" + java.time.LocalDate.now() + ".xlsx";

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelBytes);
    }
}
