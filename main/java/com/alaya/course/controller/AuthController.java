package com.alaya.course.controller;

import com.alaya.course.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           Model model) {
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "密码不能为空");
            return "register";
        }
        boolean success = userService.register(username, password);
        if (success) {
            return "redirect:/login?registered";
        } else {
            model.addAttribute("error", "用户名已存在");
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }
}