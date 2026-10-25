package com.examportal.dto;

import com.examportal.model.QuestionType;

/**
 * DTO representing an individual question evaluation review.
 */
public class QuestionReviewDto {

    private int index;
    private Long questionId;
    private String questionText;
    private QuestionType type;
    private Integer questionMarks;
    private String studentAnswerText;
    private String correctAnswerText;
    private boolean correct;
    private boolean attempted;
    private Double marksAwarded;
    private String statusBadgeClass;
    private String statusLabel;

    public QuestionReviewDto() {
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public Integer getQuestionMarks() {
        return questionMarks;
    }

    public void setQuestionMarks(Integer questionMarks) {
        this.questionMarks = questionMarks;
    }

    public String getStudentAnswerText() {
        return studentAnswerText;
    }

    public void setStudentAnswerText(String studentAnswerText) {
        this.studentAnswerText = studentAnswerText;
    }

    public String getCorrectAnswerText() {
        return correctAnswerText;
    }

    public void setCorrectAnswerText(String correctAnswerText) {
        this.correctAnswerText = correctAnswerText;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public boolean isAttempted() {
        return attempted;
    }

    public void setAttempted(boolean attempted) {
        this.attempted = attempted;
    }

    public Double getMarksAwarded() {
        return marksAwarded;
    }

    public void setMarksAwarded(Double marksAwarded) {
        this.marksAwarded = marksAwarded;
    }

    public String getStatusBadgeClass() {
        return statusBadgeClass;
    }

    public void setStatusBadgeClass(String statusBadgeClass) {
        this.statusBadgeClass = statusBadgeClass;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }
}
