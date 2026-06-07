package com.alaya.course.dto;

import java.time.LocalDateTime;

public class EnrollmentGradeDTO {
    private Long id;
    private String courseName;
    private String studentUsername;
    private Integer score;
    private String gradeComment;
    private LocalDateTime gradedAt;
    private String status; // "已录入" / "未录入"

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getGradeComment() { return gradeComment; }
    public void setGradeComment(String gradeComment) { this.gradeComment = gradeComment; }
    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}