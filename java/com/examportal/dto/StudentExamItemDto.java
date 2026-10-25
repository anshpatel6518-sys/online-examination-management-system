package com.examportal.dto;

import java.time.LocalDateTime;

/**
 * DTO representing an exam item customized for the student portal view.
 */
public class StudentExamItemDto {

    private Long examId;
    private String title;
    private String courseCode;
    private String courseName;
    private Integer durationMinutes;
    private Integer totalMarks;
    private Integer passingMarks;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean negativeMarking;
    private Double negativeMarkValue;
    private String status; // "Upcoming", "Ongoing", "Completed"
    private boolean canStart;
    private boolean canResume;
    private boolean hasAttempted;
    private Long attemptId;
    private String buttonLabel;
    private String buttonClass;
    private boolean buttonDisabled;
    private String actionUrl;

    public StudentExamItemDto() {
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCanStart() {
        return canStart;
    }

    public void setCanStart(boolean canStart) {
        this.canStart = canStart;
    }

    public boolean isCanResume() {
        return canResume;
    }

    public void setCanResume(boolean canResume) {
        this.canResume = canResume;
    }

    public boolean isHasAttempted() {
        return hasAttempted;
    }

    public void setHasAttempted(boolean hasAttempted) {
        this.hasAttempted = hasAttempted;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public void setButtonLabel(String buttonLabel) {
        this.buttonLabel = buttonLabel;
    }

    public String getButtonClass() {
        return buttonClass;
    }

    public void setButtonClass(String buttonClass) {
        this.buttonClass = buttonClass;
    }

    public boolean isButtonDisabled() {
        return buttonDisabled;
    }

    public void setButtonDisabled(boolean buttonDisabled) {
        this.buttonDisabled = buttonDisabled;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}
