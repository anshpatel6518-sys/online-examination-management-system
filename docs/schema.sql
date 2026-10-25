-- =============================================================================
-- Online Examination Management System - Database Schema Documentation
-- Database: exam_db
-- Dialect: MySQL 8+
-- =============================================================================

CREATE DATABASE IF NOT EXISTS exam_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE exam_db;

-- -----------------------------------------------------------------------------
-- 1. Users Table
-- Roles: ADMIN, TEACHER, STUDENT
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. Courses Table
-- Represents academic subjects taught by assigned teachers
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(30) NOT NULL UNIQUE,
    description TEXT,
    teacher_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_courses_code (code),
    INDEX idx_courses_teacher (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. Exams Table
-- Scheduled exams associated with a course and created by a teacher
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    course_id BIGINT NOT NULL,
    created_by_id BIGINT NOT NULL,
    duration_minutes INT NOT NULL,
    total_marks INT NOT NULL,
    passing_marks INT NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    negative_marking BOOLEAN NOT NULL DEFAULT FALSE,
    negative_mark_value DOUBLE DEFAULT 0.0,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    shuffle_questions BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exams_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_exams_created_by FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_exams_course (course_id),
    INDEX idx_exams_published (published)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. Questions Table
-- Questions belonging directly to an exam or to a course bank
-- Types: MCQ, TRUE_FALSE
-- Difficulty: EASY, MEDIUM, HARD
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT,
    course_id BIGINT,
    text TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    marks INT NOT NULL DEFAULT 1,
    difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_questions_exam FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    CONSTRAINT fk_questions_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL,
    INDEX idx_questions_exam (exam_id),
    INDEX idx_questions_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. Question Options Table
-- Individual answer choices for a question
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS question_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    correct BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_options_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    INDEX idx_options_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. Exam Attempts Table
-- Tracks student participation in an exam session
-- Status: IN_PROGRESS, SUBMITTED
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS exam_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    exam_id BIGINT NOT NULL,
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_at DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    CONSTRAINT fk_attempts_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_attempts_exam FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    INDEX idx_attempts_student (student_id),
    INDEX idx_attempts_exam (exam_id),
    INDEX idx_attempts_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. Student Answers Table
-- Student's chosen option and review bookmark per question
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS student_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT,
    marked_for_review BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_answers_attempt FOREIGN KEY (attempt_id) REFERENCES exam_attempts(id) ON DELETE CASCADE,
    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_answers_option FOREIGN KEY (selected_option_id) REFERENCES question_options(id) ON DELETE SET NULL,
    INDEX idx_answers_attempt (attempt_id),
    INDEX idx_answers_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. Results Table
-- Automated evaluation and score breakdown per attempt
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL UNIQUE,
    score DOUBLE NOT NULL,
    total_marks DOUBLE NOT NULL,
    percentage DOUBLE NOT NULL,
    passed BOOLEAN NOT NULL,
    correct_count INT NOT NULL DEFAULT 0,
    wrong_count INT NOT NULL DEFAULT 0,
    unattempted_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_results_attempt FOREIGN KEY (attempt_id) REFERENCES exam_attempts(id) ON DELETE CASCADE,
    INDEX idx_results_attempt (attempt_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Seed Data: Default System Administrator (Password: Admin@123)
-- BCrypt Hash: $2a$10$wK8V6zC... (handled automatically by DataInitializer)
-- -----------------------------------------------------------------------------
