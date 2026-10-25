package com.examportal.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO providing complete score card and evaluation breakdown for a student attempt.
 */
public class StudentResultViewDto {

    private Long attemptId;
    private Long examId;
    private String examTitle;
    private String courseCode;
    private String courseName;
    private String studentName;
    private String studentEmail;
    private Integer durationMinutes;
    private Double score;
    private Double totalMarks;
    private Double percentage;
    private boolean passed;
    private Integer passingMarks;
    private int correctCount;
    private int wrongCount;
    private int unattemptedCount;
    private int totalQuestions;
    private LocalDateTime submittedAt;
    private boolean allowReview;
    private int tabSwitchCount;
    private List<QuestionReviewDto> reviewList = new ArrayList<>();

    public StudentResultViewDto() {
    }

    public int getTabSwitchCount() {
        return tabSwitchCount;
    }

    public void setTabSwitchCount(int tabSwitchCount) {
        this.tabSwitchCount = tabSwitchCount;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public double getAccuracyPercentage() {
        int totalAnswered = correctCount + wrongCount;
        if (totalAnswered <= 0) {
            return 0.0;
        }
        return Math.round((correctCount * 100.0 / totalAnswered) * 100.0) / 100.0;
    }

    public List<QuestionReviewDto> getQuestionReviews() {
        return reviewList;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
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

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public Integer getPassingMarks() {
        return passingMarks;
    }

    public void setPassingMarks(Integer passingMarks) {
        this.passingMarks = passingMarks;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(int wrongCount) {
        this.wrongCount = wrongCount;
    }

    public int getUnattemptedCount() {
        return unattemptedCount;
    }

    public void setUnattemptedCount(int unattemptedCount) {
        this.unattemptedCount = unattemptedCount;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public boolean isAllowReview() {
        return allowReview;
    }

    public void setAllowReview(boolean allowReview) {
        this.allowReview = allowReview;
    }

    public List<QuestionReviewDto> getReviewList() {
        return reviewList;
    }

    public void setReviewList(List<QuestionReviewDto> reviewList) {
        this.reviewList = reviewList;
    }
}
