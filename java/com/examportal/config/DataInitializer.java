package com.examportal.config;

import com.examportal.model.Role;
import com.examportal.model.User;
import com.examportal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes essential application data upon first startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed default admin accounts
        seedUserIfMissing("admin@college.edu", "System Administrator", "admin123", Role.ADMIN);
        seedUserIfMissing("admin@exam.com", "System Administrator", "Admin@123", Role.ADMIN);

        // Seed default faculty account
        seedUserIfMissing("teacher@college.edu", "Prof. Alan Turing", "teacher123", Role.TEACHER);

        // Seed default student account
        seedUserIfMissing("student@college.edu", "Alex Johnson", "student123", Role.STUDENT);
    }

    private void seedUserIfMissing(String email, String fullName, String rawPassword, Role role) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setEnabled(true);

            userRepository.save(user);
            log.info("Default {} user created successfully: {}", role, email);
        }
    }
}
