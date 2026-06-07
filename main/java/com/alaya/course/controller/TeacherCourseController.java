package com.alaya.course.controller;

import com.alaya.course.domain.Course;
import com.alaya.course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/teacher")
public class TeacherCourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/courses")
    public String listCourses(Authentication authentication, Model model) {
        String username = authentication.getName();
        model.addAttribute("courses", courseService.getTeacherCourses(username));
        return "teacher-courses";
    }

    @GetMapping("/courses/create")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("isEdit", false);
        return "course-form";
    }

    @PostMapping("/courses")
    public String createCourse(@RequestParam String name,
                               @RequestParam(required = false) String description,
                               @RequestParam Integer credit,
                               @RequestParam Integer capacity,
                               @RequestParam String schedule,
                               Authentication authentication,
                               Model model) {
        try {
            courseService.createCourse(name, description, credit, capacity, schedule, authentication.getName());
            return "redirect:/teacher/courses";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("course", new Course());
            return "course-form";
        }
    }

    @GetMapping("/courses/{id}/edit")
    public String showEditForm(@PathVariable Long id, Authentication authentication, Model model) {
        try {
            Course course = courseService.getCourseById(id);
            model.addAttribute("course", course);
            model.addAttribute("isEdit", true);
            return "course-form";
        } catch (RuntimeException e) {
            return "redirect:/teacher/courses";
        }
    }

    @PostMapping("/courses/{id}/edit")
    public String updateCourse(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam(required = false) String description,
                               @RequestParam Integer credit,
                               @RequestParam Integer capacity,
                               @RequestParam String schedule,
                               Authentication authentication,
                               Model model) {
        try {
            courseService.updateCourse(id, name, description, credit, capacity, schedule, authentication.getName());
            return "redirect:/teacher/courses";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("isEdit", true);
            try {
                Course course = courseService.getCourseById(id);
                model.addAttribute("course", course);
            } catch (RuntimeException ex) { }
            return "course-form";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("isEdit", true);
            try {
                Course course = courseService.getCourseById(id);
                model.addAttribute("course", course);
            } catch (RuntimeException ex) { }
            return "course-form";
        }
    }

    @PostMapping("/courses/{id}/delete")
    public String deleteCourse(@PathVariable Long id, Authentication authentication) {
        try {
            courseService.deleteCourse(id, authentication.getName());
        } catch (RuntimeException e) {
            // 记录日志或忽略
        }
        return "redirect:/teacher/courses";
    }
}