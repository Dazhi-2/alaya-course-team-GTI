package com.alaya.course;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("admin123 -> " + encoder.encode("admin123"));
        System.out.println("teacher123 -> " + encoder.encode("teacher123"));
    }
}