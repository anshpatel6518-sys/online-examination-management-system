package com.examportal.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for AJAX autosave requests when an option is selected or cleared.
 */
public class SaveAnswerDto {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    private Long selectedOptionId; // Nullable if clearing answer

    private Boolean markedForReview; // Nullable if not altering review state

    public SaveAnswerDto() {
    }

    public SaveAnswerDto(Long questionId, Long selectedOptionId, Boolean markedForReview) {
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.markedForReview = markedForReview;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public Boolean getMarkedForReview() {
        return markedForReview;
    }

    public void setMarkedForReview(Boolean markedForReview) {
        this.markedForReview = markedForReview;
    }
}
