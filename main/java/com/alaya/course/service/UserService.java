package com.alaya.course.service;

import com.alaya.course.domain.User;
import com.alaya.course.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean register(String username, String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        if (userRepository.existsByUsername(username)) {
            return false;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("STUDENT");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }
}