package com.examportal.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * AuthenticationSuccessHandler routing users to their specific dashboard based on role.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_ADMIN".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                return;
            } else if ("ROLE_TEACHER".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/teacher/dashboard");
                return;
            } else if ("ROLE_STUDENT".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
                return;
            }
        }
        response.sendRedirect(request.getContextPath() + "/");
    }
}
