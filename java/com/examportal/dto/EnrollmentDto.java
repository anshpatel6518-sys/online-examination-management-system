package com.examportal.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for managing student course enrollments.
 */
public class EnrollmentDto {

    private Long studentId;
    private String studentName;
    private String studentEmail;
    private List<Long> courseIds = new ArrayList<>();

    public EnrollmentDto() {
    }

    public EnrollmentDto(Long studentId, String studentName, String studentEmail, List<Long> courseIds) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.courseIds = courseIds != null ? courseIds : new ArrayList<>();
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
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

    public List<Long> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<Long> courseIds) {
        this.courseIds = courseIds != null ? courseIds : new ArrayList<>();
    }
}
