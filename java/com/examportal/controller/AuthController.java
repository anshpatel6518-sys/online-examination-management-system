package com.examportal.controller;

import com.examportal.dto.RegisterDto;
import com.examportal.model.Role;
import com.examportal.model.User;
import com.examportal.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controller handling user authentication and registration requests.
 */
@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.examportal.service.EmailService emailService;
    private final com.examportal.service.PasswordResetService passwordResetService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          com.examportal.service.EmailService emailService,
                          com.examportal.service.PasswordResetService passwordResetService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.passwordResetService = passwordResetService;
    }

    /**
     * Display the login page.
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }

    /**
     * Display the registration page.
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new RegisterDto());
        return "auth/register";
    }

    /**
     * Process registration submission.
     */
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("user") RegisterDto registerDto,
                                      BindingResult bindingResult,
                                      Model model) {
        // Verify password confirmation
        if (registerDto.getPassword() != null && !registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }

        // Verify duplicate email
        if (registerDto.getEmail() != null && userRepository.existsByEmail(registerDto.getEmail().trim().toLowerCase())) {
            bindingResult.rejectValue("email", "email.duplicate", "An account with this email already exists");
        }

        // Ensure only STUDENT or TEACHER roles can be registered
        if (registerDto.getRole() == Role.ADMIN) {
            bindingResult.rejectValue("role", "role.invalid", "Administrator registration is not permitted");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        // Create and persist new user
        User user = new User();
        user.setFullName(registerDto.getFullName().trim());
        user.setEmail(registerDto.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRole(registerDto.getRole());
        user.setEnabled(true);

        userRepository.save(user);

        // Send welcome email notification
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName(), user.getRole().name());

        return "redirect:/login?registered=true";
    }

    /**
     * Display forgot password request form.
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordDto", new com.examportal.dto.ForgotPasswordDto());
        return "auth/forgot-password";
    }

    /**
     * Process forgot password submission and generate reset token.
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("forgotPasswordDto") com.examportal.dto.ForgotPasswordDto dto,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        passwordResetService.processForgotPassword(dto.getEmail());
        return "redirect:/login?forgotSuccess=true";
    }

    /**
     * Display password reset form if token is valid.
     */
    @GetMapping("/reset-password")
    public String showResetPasswordPage(@org.springframework.web.bind.annotation.RequestParam(name = "token", required = false) String token,
                                        Model model) {
        if (token == null || !passwordResetService.validateResetToken(token)) {
            return "redirect:/login?tokenInvalid=true";
        }

        model.addAttribute("resetPasswordDto", new com.examportal.dto.ResetPasswordDto(token));
        return "auth/reset-password";
    }

    /**
     * Process new password submission.
     */
    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("resetPasswordDto") com.examportal.dto.ResetPasswordDto dto,
                                       BindingResult bindingResult) {
        if (dto.getNewPassword() != null && !dto.getNewPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }

        boolean success = passwordResetService.resetPassword(dto.getToken(), dto.getNewPassword());
        if (!success) {
            return "redirect:/login?tokenInvalid=true";
        }

        return "redirect:/login?resetSuccess=true";
    }
}
