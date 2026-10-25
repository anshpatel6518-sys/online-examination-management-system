package com.examportal.service;

import java.time.LocalDate;

public interface ExcelExportService {

    byte[] exportExamResultsToExcel(Long examId, Long teacherId);

    byte[] exportAdminReportsToExcel(Long courseId, Long examId, LocalDate fromDate, LocalDate toDate, String status);
}
