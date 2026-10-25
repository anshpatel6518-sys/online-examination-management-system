package com.examportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * Main entry point for the Online Examination Management System.
 *
 * Note: SecurityAutoConfiguration is excluded temporarily for Phase 1 setup
 * so the initial welcome page can be verified before security rules and roles are configured.
 */
@SpringBootApplication
public class OnlineExamSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineExamSystemApplication.class, args);
    }
}
