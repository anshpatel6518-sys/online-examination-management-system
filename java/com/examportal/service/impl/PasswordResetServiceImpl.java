package com.examportal.service.impl;

import com.examportal.model.PasswordResetToken;
import com.examportal.model.User;
import com.examportal.repository.PasswordResetTokenRepository;
import com.examportal.repository.UserRepository;
import com.examportal.service.EmailService;
import com.examportal.service.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetServiceImpl(UserRepository userRepository,
                                    PasswordResetTokenRepository tokenRepository,
                                    EmailService emailService,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void processForgotPassword(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());
        if (userOpt.isEmpty()) {
            log.warn("Password reset requested for non-existing email: {}", email);
            return;
        }

        User user = userOpt.get();

        // Invalidate or remove existing tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate 15-minute token
        String tokenString = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                tokenString,
                user,
                LocalDateTime.now().plusMinutes(15)
        );

        tokenRepository.save(resetToken);

        // Send reset email with token
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), tokenString);
        log.info("Generated password reset token for user {}: {}", user.getEmail(), tokenString);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenAndUsedFalse(token.trim());
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        return !resetToken.isExpired();
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank() || newPassword == null || newPassword.length() < 8) {
            return false;
        }

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenAndUsedFalse(token.trim());
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.isExpired()) {
            return false;
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password successfully reset for user {}", user.getEmail());
        return true;
    }
}
