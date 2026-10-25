package com.examportal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * DTO for creating and editing examination configurations.
 */
public class ExamDto {

    private Long id;

    @NotBlank(message = "Exam title is required")
    @Size(max = 150, message = "Exam title cannot exceed 150 characters")
    private String title;

    @NotNull(message = "Course is required")
    private Long courseId;

    @NotNull(message = "Duration in minutes is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes = 60;

    @NotNull(message = "Passing marks is required")
    @Min(value = 0, message = "Passing marks cannot be negative")
    private Integer passingMarks = 40;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    private boolean negativeMarking = false;

    private Double negativeMarkValue = 0.25;

    private boolean shuffleQuestions = false;

    private boolean showAnswerKey = true;

    public ExamDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getPassingMarks() {
        return passingMarks;
    }

    public void setPassingMarks(Integer passingMarks) {
        this.passingMarks = passingMarks;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isNegativeMarking() {
        return negativeMarking;
    }

    public void setNegativeMarking(boolean negativeMarking) {
        this.negativeMarking = negativeMarking;
    }

    public Double getNegativeMarkValue() {
        return negativeMarkValue;
    }

    public void setNegativeMarkValue(Double negativeMarkValue) {
        this.negativeMarkValue = negativeMarkValue;
    }

    public boolean isShuffleQuestions() {
        return shuffleQuestions;
    }

    public void setShuffleQuestions(boolean shuffleQuestions) {
        this.shuffleQuestions = shuffleQuestions;
    }

    public boolean isShowAnswerKey() {
        return showAnswerKey;
    }

    public void setShowAnswerKey(boolean showAnswerKey) {
        this.showAnswerKey = showAnswerKey;
    }
}
