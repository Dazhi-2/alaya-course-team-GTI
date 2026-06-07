package com.alaya.course.controller;

import com.alaya.course.domain.Enrollment;
import com.alaya.course.dto.EnrollmentGradeDTO;
import com.alaya.course.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class GradeController {

    @Autowired
    private GradeService gradeService;

    // 教师录入/修改成绩
    @PostMapping("/teacher/enrollments/{id}/grade")
    public String saveGrade(@PathVariable Long id,
                            @RequestParam("score") String scoreStr,
                            @RequestParam(required = false) String comment,
                            Authentication authentication,
                            RedirectAttributes redirectAttrs) {
        try {
            Integer score = Integer.parseInt(scoreStr);
            Enrollment enrollment = gradeService.saveOrUpdateGrade(id, score, comment, authentication.getName());
            boolean isUpdate = enrollment.getScore() != null && enrollment.getScore().equals(score);
            String message = isUpdate ? "成绩修改成功" : "成绩录入成功";
            redirectAttrs.addFlashAttribute("successMessage", message);
            return "redirect:/teacher/courses/" + enrollment.getCourse().getId() + "/students";
        } catch (NumberFormatException e) {
            redirectAttrs.addFlashAttribute("error", "成绩必须为整数");
            return "redirect:/teacher/courses";
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/teacher/courses";
        }
    }

    // 教师查看课程学生名单及成绩统计
    @GetMapping("/teacher/courses/{id}/students")
    public String courseStudents(@PathVariable Long id,
                                 Authentication authentication,
                                 Model model) {
        try {
            List<Enrollment> enrollments = gradeService.getEnrollmentsByCourse(id, authentication.getName());
            List<EnrollmentGradeDTO> dtos = enrollments.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            model.addAttribute("enrollments", dtos);
            model.addAttribute("courseId", id);
            addGradeStatistics(model, dtos);
            return "teacher/course-students";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("enrollments", Collections.emptyList());
            return "teacher/course-students";
        }
    }

    // 学生查看个人成绩
    @GetMapping("/student/grades")
    public String myGrades(Authentication authentication, Model model) {
        List<Enrollment> enrollments = gradeService.getEnrollmentsByStudent(authentication.getName());
        List<EnrollmentGradeDTO> dtos = enrollments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        model.addAttribute("enrollments", dtos);
        return "student/grades";
    }

    private EnrollmentGradeDTO toDTO(Enrollment enrollment) {
        EnrollmentGradeDTO dto = new EnrollmentGradeDTO();
        dto.setId(enrollment.getId());
        dto.setCourseName(enrollment.getCourse().getName());
        dto.setStudentUsername(enrollment.getStudent().getUsername());
        dto.setScore(enrollment.getScore());
        dto.setGradeComment(enrollment.getGradeComment());
        dto.setGradedAt(enrollment.getGradedAt());
        dto.setStatus(enrollment.getScore() != null ? "已录入" : "未录入");
        return dto;
    }

    private void addGradeStatistics(Model model, List<EnrollmentGradeDTO> dtos) {
        List<EnrollmentGradeDTO> graded = dtos.stream()
                .filter(d -> d.getScore() != null)
                .collect(Collectors.toList());
        if (!graded.isEmpty()) {
            double avg = graded.stream().mapToInt(EnrollmentGradeDTO::getScore).average().orElse(0);
            int max = graded.stream().mapToInt(EnrollmentGradeDTO::getScore).max().orElse(0);
            int min = graded.stream().mapToInt(EnrollmentGradeDTO::getScore).min().orElse(0);
            model.addAttribute("averageScore", Math.round(avg * 10.0) / 10.0);
            model.addAttribute("maxScore", max);
            model.addAttribute("minScore", min);
            model.addAttribute("gradedCount", graded.size());
        }
        // 注意：如果没有已录入成绩，则不添加这些属性（模板会通过 th:if 判断）
    }
}