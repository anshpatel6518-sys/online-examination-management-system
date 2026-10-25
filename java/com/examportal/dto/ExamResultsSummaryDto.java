package com.examportal.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO aggregating statistical summaries, distribution metrics, and candidate lists for a teacher's exam.
 */
public class ExamResultsSummaryDto {

    private Long examId;
    private String examTitle;
    private String courseCode;
    private String courseName;
    private Integer totalMarks;
    private Integer passingMarks;
    private int totalCandidates;
    private Integer totalQuestions;
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private Double passPercentage;

    // Distribution bracket counts: 0-20%, 21-40%, 41-60%, 61-80%, 81-100%
    private List<Integer> distributionCounts = new ArrayList<>();

    private List<TeacherStudentResultItemDto> studentResults = new ArrayList<>();

    public ExamResultsSummaryDto() {
    }

    public Integer getTotalQuestions() {
        return totalQuestions != null ? totalQuestions : 0;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getTotalSubmissions() {
        return totalCandidates;
    }

    public List<TeacherStudentResultItemDto> getStudents() {
        return studentResults;
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

    public int getTotalCandidates() {
        return totalCandidates;
    }

    public void setTotalCandidates(int totalCandidates) {
        this.totalCandidates = totalCandidates;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Double getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(Double highestScore) {
        this.highestScore = highestScore;
    }

    public Double getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(Double lowestScore) {
        this.lowestScore = lowestScore;
    }

    public Double getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(Double passPercentage) {
        this.passPercentage = passPercentage;
    }

    public List<Integer> getDistributionCounts() {
        return distributionCounts;
    }

    public void setDistributionCounts(List<Integer> distributionCounts) {
        this.distributionCounts = distributionCounts;
    }

    public List<TeacherStudentResultItemDto> getStudentResults() {
        return studentResults;
    }

    public void setStudentResults(List<TeacherStudentResultItemDto> studentResults) {
        this.studentResults = studentResults;
    }
}
