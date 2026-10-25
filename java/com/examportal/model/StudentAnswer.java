package com.examportal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing an individual response given by a student to an exam question.
 */
@Entity
@Table(name = "student_answers")
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    @NotNull(message = "Attempt is required")
    private ExamAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @NotNull(message = "Question is required")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    @Column(name = "marked_for_review", nullable = false)
    private boolean markedForReview = false;

    public StudentAnswer() {
    }

    public StudentAnswer(Long id, ExamAttempt attempt, Question question, QuestionOption selectedOption, boolean markedForReview) {
        this.id = id;
        this.attempt = attempt;
        this.question = question;
        this.selectedOption = selectedOption;
        this.markedForReview = markedForReview;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExamAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(ExamAttempt attempt) {
        this.attempt = attempt;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public QuestionOption getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(QuestionOption selectedOption) {
        this.selectedOption = selectedOption;
    }

    public boolean isMarkedForReview() {
        return markedForReview;
    }

    public void setMarkedForReview(boolean markedForReview) {
        this.markedForReview = markedForReview;
    }
}
