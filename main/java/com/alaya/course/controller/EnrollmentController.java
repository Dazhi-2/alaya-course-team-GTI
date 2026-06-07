package com.alaya.course.controller;

import com.alaya.course.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    // 学生选课
    @PostMapping("/student/courses/{id}/enroll")
    public String enroll(@PathVariable Long id,
                         Authentication authentication,
                         RedirectAttributes redirectAttrs) {
        try {
            enrollmentService.enroll(id, authentication.getName());
            redirectAttrs.addFlashAttribute("successMessage", "选课成功");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/courses";
    }

    // 学生退课
    @PostMapping("/student/enrollments/{id}/drop")
    public String drop(@PathVariable Long id,
                       Authentication authentication,
                       RedirectAttributes redirectAttrs) {
        try {
            enrollmentService.drop(id, authentication.getName());
            redirectAttrs.addFlashAttribute("successMessage", "退课成功");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/schedule";
    }

    // 个人课表
    @GetMapping("/student/schedule")
    public String schedule(Authentication authentication, Model model) {
        var enrollments = enrollmentService.getStudentSchedule(authentication.getName());
        model.addAttribute("enrollments", enrollments);
        return "my-schedule";
    }
}