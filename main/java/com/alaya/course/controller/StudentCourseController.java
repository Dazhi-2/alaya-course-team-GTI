package com.alaya.course.controller;

import com.alaya.course.domain.Course;
import com.alaya.course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/student")
public class StudentCourseController {

    private static final int PAGE_SIZE = 10;

    @Autowired
    private CourseService courseService;

    @GetMapping("/courses")
    public String listCourses(@RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {
        if (page < 0) page = 0;
        Page<Course> coursePage = courseService.searchCourses(keyword, PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "course-list";
    }

    @GetMapping("/courses/{id}")
    public String courseDetail(@PathVariable Long id, Model model) {
        try {
            Course course = courseService.getCourseById(id);
            model.addAttribute("course", course);
            return "course-detail";
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "课程不存在");
        }
    }
}