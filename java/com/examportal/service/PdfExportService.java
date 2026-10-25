package com.examportal.service;

public interface PdfExportService {

    byte[] generateStudentResultPdf(Long attemptId, Long studentId);
}
