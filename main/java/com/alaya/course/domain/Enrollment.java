package com.alaya.course.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private LocalDateTime enrolledAt;

    private boolean active = true;

    // === 迭代4新增：成绩相关字段（简化版设计） ===
    @Column(name = "score")
    private Integer score;  // 百分制成绩（0-100），null表示未录入

    @Column(name = "grade_comment", length = 255)
    private String gradeComment;  // 教师评语

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;  // 录入/修改时间

    // 辅助方法：是否已录入成绩
    public boolean isGraded() {
        return score != null;
    }

    // getter/setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getGradeComment() { return gradeComment; }
    public void setGradeComment(String gradeComment) { this.gradeComment = gradeComment; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
}