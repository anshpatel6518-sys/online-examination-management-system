package com.examportal.dto;

import com.examportal.model.Difficulty;
import com.examportal.model.QuestionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for creating and editing question bank items.
 */
public class QuestionDto {

    private Long id;

    @NotNull(message = "Course is required")
    private Long courseId;

    @NotBlank(message = "Question text is required")
    private String text;

    @NotNull(message = "Question type is required")
    private QuestionType type = QuestionType.MCQ;

    @NotNull(message = "Marks value is required")
    @Min(value = 1, message = "Marks must be at least 1")
    private Integer marks = 1;

    @NotNull(message = "Difficulty level is required")
    private Difficulty difficulty = Difficulty.MEDIUM;

    /**
     * Text for each option.
     * For MCQ: 4 options.
     * For TRUE_FALSE: 2 options (True, False).
     */
    private List<String> options = new ArrayList<>();

    /**
     * 0-based index of the correct option.
     */
    @NotNull(message = "You must select one correct option")
    private Integer correctOptionIndex;

    public QuestionDto() {
        // Initialize with default 4 empty options for MCQ
        this.options.add("");
        this.options.add("");
        this.options.add("");
        this.options.add("");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
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

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public Integer getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public void setCorrectOptionIndex(Integer correctOptionIndex) {
        this.correctOptionIndex = correctOptionIndex;
    }
}
