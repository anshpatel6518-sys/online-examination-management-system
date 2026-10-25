package com.examportal.service.impl;

import com.examportal.dto.AdminReportItemDto;
import com.examportal.dto.AdminReportSummaryDto;
import com.examportal.dto.ExamResultsSummaryDto;
import com.examportal.dto.TeacherStudentResultItemDto;
import com.examportal.service.EvaluationService;
import com.examportal.service.ExcelExportService;
import com.examportal.service.ReportService;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ExcelExportServiceImpl implements ExcelExportService {

    private final EvaluationService evaluationService;
    private final ReportService reportService;

    public ExcelExportServiceImpl(EvaluationService evaluationService, ReportService reportService) {
        this.evaluationService = evaluationService;
        this.reportService = reportService;
    }

    @Override
    public byte[] exportExamResultsToExcel(Long examId, Long teacherId) {
        ExamResultsSummaryDto summary = evaluationService.getTeacherExamResults(examId, teacherId);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Results - " + sanitizeSheetName(summary.getExamTitle()));
            sheet.setDisplayGridlines(true);

            // Palette
            byte[] charcoalRgb = new byte[]{33, 37, 41};
            byte[] lightGreyRgb = new byte[]{(byte) 248, (byte) 249, (byte) 250};
            byte[] borderGreyRgb = new byte[]{(byte) 222, (byte) 226, (byte) 230};
            byte[] passGreenRgb = new byte[]{25, (byte) 135, 84};
            byte[] failRedRgb = new byte[]{(byte) 220, 53, 69};

            // Fonts
            XSSFFont titleFont = workbook.createFont();
            titleFont.setFontName("Calibri");
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setBold(true);

            XSSFFont boldFont = workbook.createFont();
            boldFont.setFontName("Calibri");
            boldFont.setFontHeightInPoints((short) 11);
            boldFont.setBold(true);

            XSSFFont headerFont = workbook.createFont();
            headerFont.setFontName("Calibri");
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            XSSFFont regularFont = workbook.createFont();
            regularFont.setFontName("Calibri");
            regularFont.setFontHeightInPoints((short) 11);

            XSSFFont passFont = workbook.createFont();
            passFont.setFontName("Calibri");
            passFont.setFontHeightInPoints((short) 11);
            passFont.setBold(true);
            passFont.setColor(new XSSFColor(passGreenRgb, null));

            XSSFFont failFont = workbook.createFont();
            failFont.setFontName("Calibri");
            failFont.setFontHeightInPoints((short) 11);
            failFont.setBold(true);
            failFont.setColor(new XSSFColor(failRedRgb, null));

            // Styles
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(new XSSFColor(charcoalRgb, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setCellBorders(headerStyle);

            XSSFCellStyle regularCellStyle = workbook.createCellStyle();
            regularCellStyle.setFont(regularFont);
            regularCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setCellBorders(regularCellStyle);

            XSSFCellStyle centerCellStyle = workbook.createCellStyle();
            centerCellStyle.setFont(regularFont);
            centerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            centerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setCellBorders(centerCellStyle);

            XSSFCellStyle boldCenterCellStyle = workbook.createCellStyle();
            boldCenterCellStyle.setFont(boldFont);
            boldCenterCellStyle.setAlignment(HorizontalAlignment.CENTER);
            boldCenterCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setCellBorders(boldCenterCellStyle);

            XSSFCellStyle passCellStyle = workbook.createCellStyle();
            passCellStyle.setFont(passFont);
            passCellStyle.setAlignment(HorizontalAlignment.CENTER);
            passCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setCellBorders(passCellStyle);

            XSSFCellStyle failCellStyle = workbook.createCellStyle();
            failCellStyle.setFont(failFont);
            failCellStyle.setAlignment(HorizontalAlignment.CENTER);
            failCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setCellBorders(failCellStyle);

            XSSFCellStyle statLabelStyle = workbook.createCellStyle();
            statLabelStyle.setFont(boldFont);
            statLabelStyle.setFillForegroundColor(new XSSFColor(lightGreyRgb, null));
            statLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setCellBorders(statLabelStyle);

            int rowIdx = 0;

            // Sheet Title
            XSSFRow titleRow = sheet.createRow(rowIdx++);
            XSSFCell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Examination Results Report: " + summary.getExamTitle());
            XSSFCellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            XSSFRow subtitleRow = sheet.createRow(rowIdx++);
            subtitleRow.createCell(0).setCellValue("Course: " + summary.getCourseName() + " (" + summary.getCourseCode() + ")");

            rowIdx++; // Empty row

            // Summary Stats Block (2 columns)
            rowIdx = addStatRow(sheet, rowIdx, "Total Questions", String.valueOf(summary.getTotalQuestions()), "Total Marks", String.format("%.2f", summary.getTotalMarks()), statLabelStyle, regularCellStyle);
            rowIdx = addStatRow(sheet, rowIdx, "Passing Marks", String.valueOf(summary.getPassingMarks()), "Total Submissions", String.valueOf(summary.getTotalSubmissions()), statLabelStyle, regularCellStyle);
            rowIdx = addStatRow(sheet, rowIdx, "Average Score", String.format("%.2f", summary.getAverageScore()), "Pass Percentage", String.format("%.2f%%", summary.getPassPercentage()), statLabelStyle, regularCellStyle);
            rowIdx = addStatRow(sheet, rowIdx, "Highest Score", String.format("%.2f", summary.getHighestScore()), "Lowest Score", String.format("%.2f", summary.getLowestScore()), statLabelStyle, regularCellStyle);

            rowIdx++; // Empty row

            // Data Table Headers
            String[] headers = {
                    "Rank", "Student Name", "Email Address", "Score",
                    "Total Marks", "Percentage", "Status", "Correct",
                    "Wrong", "Unattempted", "Submitted At"
            };

            XSSFRow headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate Student Data Rows
            if (summary.getStudents() != null && !summary.getStudents().isEmpty()) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                for (TeacherStudentResultItemDto s : summary.getStudents()) {
                    XSSFRow row = sheet.createRow(rowIdx++);
                    row.setHeightInPoints(20);

                    // Rank
                    XSSFCell c0 = row.createCell(0);
                    c0.setCellValue(s.getRank());
                    c0.setCellStyle(boldCenterCellStyle);

                    // Name
                    XSSFCell c1 = row.createCell(1);
                    c1.setCellValue(s.getStudentName());
                    c1.setCellStyle(regularCellStyle);

                    // Email
                    XSSFCell c2 = row.createCell(2);
                    c2.setCellValue(s.getStudentEmail());
                    c2.setCellStyle(regularCellStyle);

                    // Score
                    XSSFCell c3 = row.createCell(3);
                    c3.setCellValue(s.getScore());
                    c3.setCellStyle(boldCenterCellStyle);

                    // Total Marks
                    XSSFCell c4 = row.createCell(4);
                    c4.setCellValue(summary.getTotalMarks());
                    c4.setCellStyle(centerCellStyle);

                    // Percentage
                    XSSFCell c5 = row.createCell(5);
                    c5.setCellValue(s.getPercentage() + "%");
                    c5.setCellStyle(boldCenterCellStyle);

                    // Status
                    XSSFCell c6 = row.createCell(6);
                    c6.setCellValue(s.isPassed() ? "PASS" : "FAIL");
                    c6.setCellStyle(s.isPassed() ? passCellStyle : failCellStyle);

                    // Correct
                    XSSFCell c7 = row.createCell(7);
                    c7.setCellValue(s.getCorrectCount());
                    c7.setCellStyle(centerCellStyle);

                    // Wrong
                    XSSFCell c8 = row.createCell(8);
                    c8.setCellValue(s.getWrongCount());
                    c8.setCellStyle(centerCellStyle);

                    // Unattempted
                    XSSFCell c9 = row.createCell(9);
                    c9.setCellValue(s.getUnattemptedCount());
                    c9.setCellStyle(centerCellStyle);

                    // Submitted At
                    XSSFCell c10 = row.createCell(10);
                    c10.setCellValue(s.getSubmittedAt() != null ? s.getSubmittedAt().format(dtf) : "N/A");
                    c10.setCellStyle(centerCellStyle);
                }
            } else {
                XSSFRow row = sheet.createRow(rowIdx++);
                XSSFCell cell = row.createCell(0);
                cell.setCellValue("No student submissions recorded yet.");
                cell.setCellStyle(regularCellStyle);
            }

            // Adjust column widths
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 3200));
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export exam results to Excel: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] exportAdminReportsToExcel(Long courseId, Long examId, LocalDate fromDate, LocalDate toDate, String status) {
        AdminReportSummaryDto summary = reportService.generateAdminReport(courseId, examId, fromDate, toDate, status);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Comprehensive Exam Report");
            sheet.setDisplayGridlines(true);

            byte[] charcoalRgb = new byte[]{33, 37, 41};
            byte[] lightGreyRgb = new byte[]{(byte) 248, (byte) 249, (byte) 250};
            byte[] passGreenRgb = new byte[]{25, (byte) 135, 84};
            byte[] failRedRgb = new byte[]{(byte) 220, 53, 69};

            XSSFFont titleFont = workbook.createFont();
            titleFont.setFontName("Calibri");
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setBold(true);

            XSSFFont boldFont = workbook.createFont();
            boldFont.setFontName("Calibri");
            boldFont.setFontHeightInPoints((short) 11);
            boldFont.setBold(true);

            XSSFFont headerFont = workbook.createFont();
            headerFont.setFontName("Calibri");
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            XSSFFont regularFont = workbook.createFont();
            regularFont.setFontName("Calibri");
            regularFont.setFontHeightInPoints((short) 11);

            XSSFFont passFont = workbook.createFont();
            passFont.setFontName("Calibri");
            passFont.setFontHeightInPoints((short) 11);
            passFont.setBold(true);
            passFont.setColor(new XSSFColor(passGreenRgb, null));

            XSSFFont failFont = workbook.createFont();
            failFont.setFontName("Calibri");
            failFont.setFontHeightInPoints((short) 11);
            failFont.setBold(true);
            failFont.setColor(new XSSFColor(failRedRgb, null));

            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(new XSSFColor(charcoalRgb, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setCellBorders(headerStyle);

            XSSFCellStyle regularCellStyle = workbook.createCellStyle();
            regularCellStyle.setFont(regularFont);
            setCellBorders(regularCellStyle);

            XSSFCellStyle centerCellStyle = workbook.createCellStyle();
            centerCellStyle.setFont(regularFont);
            centerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            setCellBorders(centerCellStyle);

            XSSFCellStyle boldCenterCellStyle = workbook.createCellStyle();
            boldCenterCellStyle.setFont(boldFont);
            boldCenterCellStyle.setAlignment(HorizontalAlignment.CENTER);
            setCellBorders(boldCenterCellStyle);

            XSSFCellStyle passCellStyle = workbook.createCellStyle();
            passCellStyle.setFont(passFont);
            passCellStyle.setAlignment(HorizontalAlignment.CENTER);
            setCellBorders(passCellStyle);

            XSSFCellStyle failCellStyle = workbook.createCellStyle();
            failCellStyle.setFont(failFont);
            failCellStyle.setAlignment(HorizontalAlignment.CENTER);
            setCellBorders(failCellStyle);

            XSSFCellStyle statLabelStyle = workbook.createCellStyle();
            statLabelStyle.setFont(boldFont);
            statLabelStyle.setFillForegroundColor(new XSSFColor(lightGreyRgb, null));
            statLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setCellBorders(statLabelStyle);

            int rowIdx = 0;

            // Sheet Title
            XSSFRow titleRow = sheet.createRow(rowIdx++);
            XSSFCell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Administrator Performance & Examination Report");
            XSSFCellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            XSSFRow subtitleRow = sheet.createRow(rowIdx++);
            subtitleRow.createCell(0).setCellValue("Generated on " + LocalDate.now());

            rowIdx++; // Empty row

            // Summary Stats Block
            rowIdx = addStatRow(sheet, rowIdx, "Total Attempts", String.valueOf(summary.getTotalAttempts()), "Average Score", String.format("%.2f", summary.getAverageScore()), statLabelStyle, regularCellStyle);
            rowIdx = addStatRow(sheet, rowIdx, "Total Passed", String.valueOf(summary.getPassCount()), "Pass Rate", String.format("%.2f%%", summary.getPassPercentage()), statLabelStyle, regularCellStyle);
            rowIdx = addStatRow(sheet, rowIdx, "Total Failed", String.valueOf(summary.getFailCount()), "Status Filter", (status != null && !status.isBlank() ? status.toUpperCase() : "ALL"), statLabelStyle, regularCellStyle);

            rowIdx++; // Empty row

            // Data Table Headers
            String[] headers = {
                    "Student Name", "Student Email", "Course Code", "Course Name",
                    "Exam Title", "Score", "Total Marks", "Percentage",
                    "Status", "Correct", "Wrong", "Unattempted", "Date & Time"
            };

            XSSFRow headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate Report Data Rows
            if (summary.getItems() != null && !summary.getItems().isEmpty()) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                for (AdminReportItemDto item : summary.getItems()) {
                    XSSFRow row = sheet.createRow(rowIdx++);
                    row.setHeightInPoints(20);

                    // Student Name
                    XSSFCell c0 = row.createCell(0);
                    c0.setCellValue(item.getStudentName());
                    c0.setCellStyle(regularCellStyle);

                    // Email
                    XSSFCell c1 = row.createCell(1);
                    c1.setCellValue(item.getStudentEmail());
                    c1.setCellStyle(regularCellStyle);

                    // Course Code
                    XSSFCell c2 = row.createCell(2);
                    c2.setCellValue(item.getCourseCode());
                    c2.setCellStyle(boldCenterCellStyle);

                    // Course Name
                    XSSFCell c3 = row.createCell(3);
                    c3.setCellValue(item.getCourseName());
                    c3.setCellStyle(regularCellStyle);

                    // Exam Title
                    XSSFCell c4 = row.createCell(4);
                    c4.setCellValue(item.getExamTitle());
                    c4.setCellStyle(regularCellStyle);

                    // Score
                    XSSFCell c5 = row.createCell(5);
                    c5.setCellValue(item.getScore());
                    c5.setCellStyle(boldCenterCellStyle);

                    // Total Marks
                    XSSFCell c6 = row.createCell(6);
                    c6.setCellValue(item.getTotalMarks());
                    c6.setCellStyle(centerCellStyle);

                    // Percentage
                    XSSFCell c7 = row.createCell(7);
                    c7.setCellValue(item.getPercentage() + "%");
                    c7.setCellStyle(boldCenterCellStyle);

                    // Status
                    XSSFCell c8 = row.createCell(8);
                    c8.setCellValue(item.getPassed() ? "PASS" : "FAIL");
                    c8.setCellStyle(item.getPassed() ? passCellStyle : failCellStyle);

                    // Correct
                    XSSFCell c9 = row.createCell(9);
                    c9.setCellValue(item.getCorrectCount() != null ? item.getCorrectCount() : 0);
                    c9.setCellStyle(centerCellStyle);

                    // Wrong
                    XSSFCell c10 = row.createCell(10);
                    c10.setCellValue(item.getWrongCount() != null ? item.getWrongCount() : 0);
                    c10.setCellStyle(centerCellStyle);

                    // Unattempted
                    XSSFCell c11 = row.createCell(11);
                    c11.setCellValue(item.getUnattemptedCount() != null ? item.getUnattemptedCount() : 0);
                    c11.setCellStyle(centerCellStyle);

                    // Date & Time
                    XSSFCell c12 = row.createCell(12);
                    c12.setCellValue(item.getSubmittedAt() != null ? item.getSubmittedAt().format(dtf) : "N/A");
                    c12.setCellStyle(centerCellStyle);
                }
            } else {
                XSSFRow row = sheet.createRow(rowIdx++);
                XSSFCell cell = row.createCell(0);
                cell.setCellValue("No records found matching the filter criteria.");
                cell.setCellStyle(regularCellStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 3200));
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export admin reports to Excel: " + e.getMessage(), e);
        }
    }

    private int addStatRow(XSSFSheet sheet, int rowIdx, String label1, String val1, String label2, String val2,
                           XSSFCellStyle labelStyle, XSSFCellStyle valStyle) {
        XSSFRow row = sheet.createRow(rowIdx++);
        row.setHeightInPoints(20);

        XSSFCell c0 = row.createCell(0);
        c0.setCellValue(label1);
        c0.setCellStyle(labelStyle);

        XSSFCell c1 = row.createCell(1);
        c1.setCellValue(val1);
        c1.setCellStyle(valStyle);

        XSSFCell c2 = row.createCell(2);
        c2.setCellValue(label2);
        c2.setCellStyle(labelStyle);

        XSSFCell c3 = row.createCell(3);
        c3.setCellValue(val2);
        c3.setCellStyle(valStyle);

        return rowIdx;
    }

    private void setCellBorders(XSSFCellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private String sanitizeSheetName(String name) {
        if (name == null || name.isBlank()) {
            return "Exam";
        }
        String sanitized = name.replaceAll("[\\\\/*?\\[\\]]", "");
        return sanitized.length() > 25 ? sanitized.substring(0, 25) : sanitized;
    }
}
