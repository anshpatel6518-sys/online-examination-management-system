package com.examportal.service.impl;

import com.examportal.dto.QuestionReviewDto;
import com.examportal.dto.StudentResultViewDto;
import com.examportal.service.EvaluationService;
import com.examportal.service.PdfExportService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfExportServiceImpl implements PdfExportService {

    private final EvaluationService evaluationService;

    public PdfExportServiceImpl(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @Override
    public byte[] generateStudentResultPdf(Long attemptId, Long studentId) {
        StudentResultViewDto dto = evaluationService.getStudentResult(attemptId, studentId);

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Palette
            Color charcoal = new Color(33, 37, 41);
            Color darkGrey = new Color(73, 80, 87);
            Color lightGrey = new Color(248, 249, 250);
            Color borderColor = new Color(222, 226, 230);
            Color passGreen = new Color(25, 135, 84);
            Color failRed = new Color(220, 53, 69);

            // Fonts
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, charcoal);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 10, darkGrey);
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, charcoal);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, charcoal);
            Font cellBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, charcoal);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, charcoal);
            Font passFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, passGreen);
            Font failFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, failRed);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, darkGrey);

            // Portal Header
            Paragraph title = new Paragraph("ONLINE EXAMINATION SYSTEM", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(2);
            document.add(title);

            Paragraph subtitle = new Paragraph("Official Student Examination Scorecard", subHeaderFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(18);
            document.add(subtitle);

            // Student & Exam Information Section
            Paragraph infoSectionTitle = new Paragraph("EXAMINATION DETAILS", sectionTitleFont);
            infoSectionTitle.setSpacingAfter(6);
            document.add(infoSectionTitle);

            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{22f, 28f, 22f, 28f});
            infoTable.setSpacingAfter(15);

            addInfoCell(infoTable, "Student Name:", cellBoldFont, lightGrey, borderColor);
            addInfoCell(infoTable, dto.getStudentName(), cellFont, Color.WHITE, borderColor);
            addInfoCell(infoTable, "Student Email:", cellBoldFont, lightGrey, borderColor);
            addInfoCell(infoTable, dto.getStudentEmail(), cellFont, Color.WHITE, borderColor);

            addInfoCell(infoTable, "Course:", cellBoldFont, lightGrey, borderColor);
            addInfoCell(infoTable, dto.getCourseName() + " (" + dto.getCourseCode() + ")", cellFont, Color.WHITE, borderColor);
            addInfoCell(infoTable, "Exam Title:", cellBoldFont, lightGrey, borderColor);
            addInfoCell(infoTable, dto.getExamTitle(), cellFont, Color.WHITE, borderColor);

            String submittedDateStr = (dto.getSubmittedAt() != null)
                    ? dto.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
                    : "N/A";

            addInfoCell(infoTable, "Submission Date:", cellBoldFont, lightGrey, borderColor);
            addInfoCell(infoTable, submittedDateStr, cellFont, Color.WHITE, borderColor);
            addInfoCell(infoTable, "Duration:", cellBoldFont, lightGrey, borderColor);
            addInfoCell(infoTable, dto.getDurationMinutes() + " Minutes", cellFont, Color.WHITE, borderColor);

            document.add(infoTable);

            // Score Card Section
            Paragraph scoreSectionTitle = new Paragraph("RESULT & PERFORMANCE SUMMARY", sectionTitleFont);
            scoreSectionTitle.setSpacingAfter(6);
            document.add(scoreSectionTitle);

            PdfPTable scoreTable = new PdfPTable(6);
            scoreTable.setWidthPercentage(100);
            scoreTable.setWidths(new float[]{18f, 16f, 16f, 16f, 16f, 18f});
            scoreTable.setSpacingAfter(15);

            addTableHeaderCell(scoreTable, "Score Obtained", tableHeaderFont, lightGrey, borderColor);
            addTableHeaderCell(scoreTable, "Total Marks", tableHeaderFont, lightGrey, borderColor);
            addTableHeaderCell(scoreTable, "Passing Marks", tableHeaderFont, lightGrey, borderColor);
            addTableHeaderCell(scoreTable, "Percentage", tableHeaderFont, lightGrey, borderColor);
            addTableHeaderCell(scoreTable, "Accuracy", tableHeaderFont, lightGrey, borderColor);
            addTableHeaderCell(scoreTable, "Final Status", tableHeaderFont, lightGrey, borderColor);

            addCenteredCell(scoreTable, String.format("%.2f", dto.getScore()), cellBoldFont, Color.WHITE, borderColor);
            addCenteredCell(scoreTable, String.format("%.2f", dto.getTotalMarks()), cellFont, Color.WHITE, borderColor);
            addCenteredCell(scoreTable, String.valueOf(dto.getPassingMarks()), cellFont, Color.WHITE, borderColor);
            addCenteredCell(scoreTable, String.format("%.2f%%", dto.getPercentage()), cellBoldFont, Color.WHITE, borderColor);
            addCenteredCell(scoreTable, String.format("%.2f%%", dto.getAccuracyPercentage()), cellFont, Color.WHITE, borderColor);

            // Status Badge Cell
            PdfPCell statusCell = new PdfPCell(new Phrase(dto.isPassed() ? "PASSED" : "FAILED", dto.isPassed() ? passFont : failFont));
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            statusCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            statusCell.setPadding(6);
            statusCell.setBorderColor(borderColor);
            statusCell.setBackgroundColor(dto.isPassed() ? new Color(230, 247, 237) : new Color(253, 237, 237));
            scoreTable.addCell(statusCell);

            document.add(scoreTable);

            // Question Breakdown Statistics
            PdfPTable statTable = new PdfPTable(3);
            statTable.setWidthPercentage(100);
            statTable.setWidths(new float[]{33.3f, 33.3f, 33.3f});
            statTable.setSpacingAfter(20);

            addCenteredCell(statTable, "Correct Answers: " + dto.getCorrectCount(), cellBoldFont, new Color(240, 249, 244), borderColor);
            addCenteredCell(statTable, "Wrong Answers: " + dto.getWrongCount(), cellBoldFont, new Color(254, 242, 242), borderColor);
            addCenteredCell(statTable, "Unattempted: " + dto.getUnattemptedCount(), cellBoldFont, lightGrey, borderColor);

            document.add(statTable);

            // Question-wise Review (if teacher allowed review)
            if (dto.isAllowReview() && dto.getQuestionReviews() != null && !dto.getQuestionReviews().isEmpty()) {
                Paragraph reviewSectionTitle = new Paragraph("QUESTION-BY-QUESTION REVIEW", sectionTitleFont);
                reviewSectionTitle.setSpacingAfter(6);
                document.add(reviewSectionTitle);

                PdfPTable reviewTable = new PdfPTable(5);
                reviewTable.setWidthPercentage(100);
                reviewTable.setWidths(new float[]{6f, 40f, 22f, 22f, 10f});
                reviewTable.setSpacingAfter(15);

                addTableHeaderCell(reviewTable, "#", tableHeaderFont, lightGrey, borderColor);
                addTableHeaderCell(reviewTable, "Question", tableHeaderFont, lightGrey, borderColor);
                addTableHeaderCell(reviewTable, "Your Answer", tableHeaderFont, lightGrey, borderColor);
                addTableHeaderCell(reviewTable, "Correct Answer", tableHeaderFont, lightGrey, borderColor);
                addTableHeaderCell(reviewTable, "Marks", tableHeaderFont, lightGrey, borderColor);

                int qNum = 1;
                for (QuestionReviewDto q : dto.getQuestionReviews()) {
                    addCenteredCell(reviewTable, String.valueOf(qNum++), cellBoldFont, Color.WHITE, borderColor);

                    PdfPCell qCell = new PdfPCell(new Phrase(q.getQuestionText(), cellFont));
                    qCell.setPadding(5);
                    qCell.setBorderColor(borderColor);
                    reviewTable.addCell(qCell);

                    PdfPCell ansCell = new PdfPCell(new Phrase(q.getStudentAnswerText() != null ? q.getStudentAnswerText() : "Not Attempted", cellFont));
                    ansCell.setPadding(5);
                    ansCell.setBorderColor(borderColor);
                    reviewTable.addCell(ansCell);

                    PdfPCell correctCell = new PdfPCell(new Phrase(q.getCorrectAnswerText() != null ? q.getCorrectAnswerText() : "-", cellFont));
                    correctCell.setPadding(5);
                    correctCell.setBorderColor(borderColor);
                    reviewTable.addCell(correctCell);

                    String marksDisplay = String.format("%s%s", (q.getMarksAwarded() != null && q.getMarksAwarded() > 0 ? "+" : ""), q.getMarksAwarded());
                    addCenteredCell(reviewTable, marksDisplay, cellBoldFont, Color.WHITE, borderColor);
                }

                document.add(reviewTable);
            }

            // Footer Timestamp
            Paragraph footer = new Paragraph("Generated on "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a"))
                    + " | Online Examination Management System", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(15);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error occurred while generating result PDF: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addInfoCell(PdfPTable table, String text, Font font, Color bgColor, Color borderColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(borderColor);
        cell.setPadding(6);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addTableHeaderCell(PdfPTable table, String text, Font font, Color bgColor, Color borderColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(borderColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addCenteredCell(PdfPTable table, String text, Font font, Color bgColor, Color borderColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(borderColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }
}
