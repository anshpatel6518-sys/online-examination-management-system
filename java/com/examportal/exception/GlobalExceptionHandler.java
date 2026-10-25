package com.examportal.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler providing friendly user-facing error pages
 * and centralized logging without leaking sensitive internals.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDeniedException(AccessDeniedException ex, Model model) {
        log.warn("Access denied: {}", ex.getMessage());
        model.addAttribute("errorMessage", "Access Denied: You do not possess the required permissions to view this resource.");
        return "error/403";
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleSecurityException(SecurityException ex, Model model) {
        log.warn("Security exception encountered: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/403";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException(NoResourceFoundException ex, Model model) {
        model.addAttribute("errorMessage", "The requested resource could not be found.");
        return "error/404";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        log.warn("Illegal argument error: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalStateException(IllegalStateException ex, Model model) {
        log.warn("Illegal state error: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unhandled server exception: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", "An internal error occurred. Please contact administrator or try again later.");
        return "error/500";
    }
}
