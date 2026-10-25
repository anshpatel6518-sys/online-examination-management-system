package com.examportal.service;

import com.examportal.dto.AdminReportSummaryDto;

import java.time.LocalDate;

public interface ReportService {

    AdminReportSummaryDto generateAdminReport(Long courseId, Long examId, LocalDate fromDate, LocalDate toDate, String status);
}
