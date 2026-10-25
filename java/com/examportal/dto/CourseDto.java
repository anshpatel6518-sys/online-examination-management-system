package com.examportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating and editing course entities.
 */
public class CourseDto {

    private Long id;

    @NotBlank(message = "Course name is required")
    @Size(max = 150, message = "Course name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Course code is required")
    @Size(max = 30, message = "Course code must not exceed 30 characters")
    private String code;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Long teacherId;

    public CourseDto() {
    }

    public CourseDto(Long id, String name, String code, String description, Long teacherId) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.teacherId = teacherId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
}
