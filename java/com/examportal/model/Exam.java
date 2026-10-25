package com.examportal.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an examination scheduled for a course.
 */
@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Exam title is required")
    @Size(max = 150, message = "Exam title must not exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @NotNull(message = "Course is required")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @NotNull(message = "Duration in minutes is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @NotNull(message = "Total marks is required")
    @Min(value = 0, message = "Total marks must be non-negative")
    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks = 0;

    @NotNull(message = "Passing marks is required")
    @Min(value = 0, message = "Passing marks must be non-negative")
    @Column(name = "passing_marks", nullable = false)
    private Integer passingMarks = 0;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "negative_marking", nullable = false)
    private boolean negativeMarking = false;

    @Column(name = "negative_mark_value")
    private Double negativeMarkValue = 0.0;

    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "shuffle_questions", nullable = false)
    private boolean shuffleQuestions = false;

    @Column(name = "show_answer_key", nullable = false)
    private boolean showAnswerKey = true;

    /**
     * Questions linked to this exam from the course question bank.
     */
    @ManyToMany
    @JoinTable(
        name = "exam_questions",
        joinColumns = @JoinColumn(name = "exam_id"),
        inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Exam() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.negativeMarkValue == null) {
            this.negativeMarkValue = 0.0;
        }
        if (this.totalMarks == null) {
            this.totalMarks = 0;
        }
        if (this.passingMarks == null) {
            this.passingMarks = 0;
        }
    }

    /**
     * Compute current status based on published state and schedule.
     */
    public String getStatus() {
        if (!this.published) {
            return "Draft";
        }
        LocalDateTime now = LocalDateTime.now();
        if (this.startTime != null && now.isBefore(this.startTime)) {
            return "Scheduled";
        }
        if (this.endTime != null && now.isAfter(this.endTime)) {
            return "Completed";
        }
        return "Ongoing";
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

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
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

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
