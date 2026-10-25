package com.examportal.service;

public interface PasswordResetService {

    void processForgotPassword(String email);

    boolean validateResetToken(String token);

    boolean resetPassword(String token, String newPassword);
}
