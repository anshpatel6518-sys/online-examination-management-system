package com.examportal.service;

/**
 * Service for sending transactional emails (welcome, exam results, password reset).
 */
public interface EmailService {

    void sendWelcomeEmail(String toEmail, String fullName, String role);

    void sendResultEmail(String toEmail, String fullName, String examTitle, String courseName,
                         double score, double totalMarks, double percentage, boolean passed, Long attemptId);

    void sendPasswordResetEmail(String toEmail, String fullName, String resetToken);
}
