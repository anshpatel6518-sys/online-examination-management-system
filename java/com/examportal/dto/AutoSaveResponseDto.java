package com.examportal.dto;

/**
 * DTO returned as JSON response to AJAX autosave operations.
 */
public class AutoSaveResponseDto {

    private boolean success;
    private long remainingSeconds;
    private boolean timeExpired;
    private String message;
    private int answeredCount;
    private int unansweredCount;
    private int markedForReviewCount;

    public AutoSaveResponseDto() {
    }

    public AutoSaveResponseDto(boolean success, long remainingSeconds, boolean timeExpired, String message) {
        this.success = success;
        this.remainingSeconds = remainingSeconds;
        this.timeExpired = timeExpired;
        this.message = message;
    }

    public AutoSaveResponseDto(boolean success, long remainingSeconds, boolean timeExpired, String message,
                               int answeredCount, int unansweredCount, int markedForReviewCount) {
        this.success = success;
        this.remainingSeconds = remainingSeconds;
        this.timeExpired = timeExpired;
        this.message = message;
        this.answeredCount = answeredCount;
        this.unansweredCount = unansweredCount;
        this.markedForReviewCount = markedForReviewCount;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public boolean isTimeExpired() {
        return timeExpired;
    }

    public void setTimeExpired(boolean timeExpired) {
        this.timeExpired = timeExpired;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getAnsweredCount() {
        return answeredCount;
    }

    public void setAnsweredCount(int answeredCount) {
        this.answeredCount = answeredCount;
    }

    public int getUnansweredCount() {
        return unansweredCount;
    }

    public void setUnansweredCount(int unansweredCount) {
        this.unansweredCount = unansweredCount;
    }

    public int getMarkedForReviewCount() {
        return markedForReviewCount;
    }

    public void setMarkedForReviewCount(int markedForReviewCount) {
        this.markedForReviewCount = markedForReviewCount;
    }
}
