package com.alaya.course.repository;

import com.alaya.course.domain.Course;
import com.alaya.course.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacher(User teacher);
    Optional<Course> findByIdAndTeacher(Long id, User teacher);
    Page<Course> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Course> findAll(Pageable pageable);
}