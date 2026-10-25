package com.examportal.dto;

/**
 * Data Transfer Object holding dashboard summary metrics.
 */
public class AdminStatsDto {

    private long totalStudents;
    private long totalTeachers;
    private long totalCourses;
    private long totalExams;

    public AdminStatsDto() {
    }

    public AdminStatsDto(long totalStudents, long totalTeachers, long totalCourses, long totalExams) {
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.totalCourses = totalCourses;
        this.totalExams = totalExams;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalTeachers() {
        return totalTeachers;
    }

    public void setTotalTeachers(long totalTeachers) {
        this.totalTeachers = totalTeachers;
    }

    public long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public long getTotalExams() {
        return totalExams;
    }

    public void setTotalExams(long totalExams) {
        this.totalExams = totalExams;
    }
}
