package com.examportal.service.impl;

import com.examportal.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:no-reply@examportal.edu}")
    private String fromEmail;

    @Value("${app.app-url:http://localhost:8080}")
    private String appUrl;

    @Override
    public void sendWelcomeEmail(String toEmail, String fullName, String role) {
        String subject = "Welcome to Online Examination System";
        String content = "Hello " + fullName + ",\n\n"
                + "Welcome to the Online Examination System! Your account has been successfully created as a " + role + ".\n\n"
                + "You can access your portal at: " + appUrl + "/login\n\n"
                + "Best regards,\n"
                + "Online Examination Management Team";

        sendEmail(toEmail, subject, content);
    }

    @Override
    public void sendResultEmail(String toEmail, String fullName, String examTitle, String courseName,
                                double score, double totalMarks, double percentage, boolean passed, Long attemptId) {
        String status = passed ? "PASSED" : "FAILED";
        String subject = "Exam Result: " + examTitle + " (" + status + ")";
        String content = "Hello " + fullName + ",\n\n"
                + "Your examination attempt has been evaluated.\n\n"
                + "Exam: " + examTitle + "\n"
                + "Course: " + courseName + "\n"
                + "Score: " + score + " / " + totalMarks + " (" + percentage + "%)\n"
                + "Status: " + status + "\n\n"
                + "You can view your detailed scorecard at:\n"
                + appUrl + "/student/results/" + attemptId + "\n\n"
                + "Best regards,\n"
                + "Online Examination Management Team";

        sendEmail(toEmail, subject, content);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetToken) {
        String subject = "Password Reset Request - Online Examination System";
        String resetLink = appUrl + "/reset-password?token=" + resetToken;
        String content = "Hello " + fullName + ",\n\n"
                + "We received a request to reset your password for the Online Examination System.\n\n"
                + "Please click the link below to set a new password:\n"
                + resetLink + "\n\n"
                + "This link will expire in 15 minutes.\n"
                + "If you did not request a password reset, you can safely ignore this email.\n\n"
                + "Best regards,\n"
                + "Online Examination Management Team";

        sendEmail(toEmail, subject, content);
    }

    private void sendEmail(String to, String subject, String body) {
        if (!mailEnabled || mailSender == null) {
            log.info("[MAIL DISABLED] Simulated email to: <{}>\nSubject: {}\nBody:\n{}\n----------------------------------------",
                    to, subject, body);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email successfully sent to <{}>: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to <{}>: {}", to, e.getMessage());
        }
    }
}
