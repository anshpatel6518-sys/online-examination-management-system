package com.examportal.service.impl;

import com.examportal.dto.AdminReportItemDto;
import com.examportal.dto.AdminReportSummaryDto;
import com.examportal.model.Exam;
import com.examportal.model.ExamAttempt;
import com.examportal.model.Result;
import com.examportal.model.User;
import com.examportal.repository.ResultRepository;
import com.examportal.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ResultRepository resultRepository;

    public ReportServiceImpl(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    @Override
    public AdminReportSummaryDto generateAdminReport(Long courseId, Long examId, LocalDate fromDate, LocalDate toDate, String status) {
        List<Result> allResults = resultRepository.findAll();
        List<AdminReportItemDto> filteredItems = new ArrayList<>();

        double sumScore = 0.0;
        int passCount = 0;

        for (Result r : allResults) {
            ExamAttempt attempt = r.getAttempt();
            if (attempt == null) {
                continue;
            }

            Exam exam = attempt.getExam();
            if (exam == null) {
                continue;
            }

            User student = attempt.getStudent();

            // Filter: course
            if (courseId != null && (exam.getCourse() == null || !courseId.equals(exam.getCourse().getId()))) {
                continue;
            }

            // Filter: exam
            if (examId != null && !examId.equals(exam.getId())) {
                continue;
            }

            // Filter: date range based on submittedAt or startedAt
            LocalDate attemptDate = (attempt.getSubmittedAt() != null)
                    ? attempt.getSubmittedAt().toLocalDate()
                    : (attempt.getStartedAt() != null ? attempt.getStartedAt().toLocalDate() : null);

            if (fromDate != null && (attemptDate == null || attemptDate.isBefore(fromDate))) {
                continue;
            }
            if (toDate != null && (attemptDate == null || attemptDate.isAfter(toDate))) {
                continue;
            }

            // Filter: status (ALL, PASS, FAIL)
            if (status != null && !status.equalsIgnoreCase("ALL") && !status.isBlank()) {
                if (status.equalsIgnoreCase("PASS") && !r.isPassed()) {
                    continue;
                }
                if (status.equalsIgnoreCase("FAIL") && r.isPassed()) {
                    continue;
                }
            }

            AdminReportItemDto item = new AdminReportItemDto();
            item.setAttemptId(attempt.getId());
            item.setStudentName(student != null ? student.getFullName() : "N/A");
            item.setStudentEmail(student != null ? student.getEmail() : "N/A");
            item.setCourseCode(exam.getCourse() != null ? exam.getCourse().getCode() : "N/A");
            item.setCourseName(exam.getCourse() != null ? exam.getCourse().getName() : "N/A");
            item.setExamTitle(exam.getTitle());
            item.setScore(r.getScore());
            item.setTotalMarks(r.getTotalMarks());
            item.setPercentage(r.getPercentage());
            item.setPassed(r.isPassed());
            item.setSubmittedAt(attempt.getSubmittedAt());
            item.setCorrectCount(r.getCorrectCount());
            item.setWrongCount(r.getWrongCount());
            item.setUnattemptedCount(r.getUnattemptedCount());

            filteredItems.add(item);

            sumScore += r.getScore();
            if (r.isPassed()) {
                passCount++;
            }
        }

        // Sort by submittedAt descending
        filteredItems.sort(Comparator.comparing(AdminReportItemDto::getSubmittedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        int totalAttempts = filteredItems.size();
        int failCount = totalAttempts - passCount;

        double passPercentage = totalAttempts > 0
                ? BigDecimal.valueOf((passCount * 100.0) / totalAttempts).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        double averageScore = totalAttempts > 0
                ? BigDecimal.valueOf(sumScore / totalAttempts).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        AdminReportSummaryDto summary = new AdminReportSummaryDto();
        summary.setTotalAttempts(totalAttempts);
        summary.setPassCount(passCount);
        summary.setFailCount(failCount);
        summary.setPassPercentage(passPercentage);
        summary.setAverageScore(averageScore);
        summary.setItems(filteredItems);

        return summary;
    }
}
