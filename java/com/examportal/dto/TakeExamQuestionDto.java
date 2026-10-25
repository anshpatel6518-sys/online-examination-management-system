package com.examportal.dto;

import com.examportal.model.QuestionOption;
import com.examportal.model.QuestionType;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing an exam question rendered in the student exam interface.
 */
public class TakeExamQuestionDto {

    private int index; // 1-based question number
    private Long questionId;
    private String text;
    private QuestionType type;
    private Integer marks;
    private List<QuestionOption> options = new ArrayList<>();
    private Long selectedOptionId;
    private boolean markedForReview;
    private boolean answered;
    private String paletteStatus; // "answered", "review", "unvisited"

    public TakeExamQuestionDto() {
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOption> options) {
        this.options = options;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
        this.answered = (selectedOptionId != null);
    }

    public boolean isMarkedForReview() {
        return markedForReview;
    }

    public void setMarkedForReview(boolean markedForReview) {
        this.markedForReview = markedForReview;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public String getPaletteStatus() {
        if (markedForReview) {
            return "review";
        } else if (answered) {
            return "answered";
        }
        return "unvisited";
    }

    public void setPaletteStatus(String paletteStatus) {
        this.paletteStatus = paletteStatus;
    }
}
