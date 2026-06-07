package com.alaya.course.service;

import com.alaya.course.domain.Course;
import com.alaya.course.domain.User;
import com.alaya.course.repository.CourseRepository;
import com.alaya.course.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Course createCourse(String name, String description, Integer credit,
                               Integer capacity, String schedule, String teacherUsername) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("课程名称不能为空");
        if (capacity == null || capacity <= 0)
            throw new IllegalArgumentException("容量必须大于0");
        if (credit == null || credit <= 0)
            throw new IllegalArgumentException("学分必须大于0");

        User teacher = userRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new RuntimeException("教师不存在"));

        Course course = new Course();
        course.setName(name.trim());
        course.setDescription(description);
        course.setCredit(credit);
        course.setCapacity(capacity);
        course.setSchedule(schedule);
        course.setTeacher(teacher);
        course.setEnrolledCount(0);
        course.setCreatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, String name, String description, Integer credit,
                               Integer capacity, String schedule, String currentUsername) {
        User teacher = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Course course = courseRepository.findByIdAndTeacher(id, teacher)
                .orElseThrow(() -> new RuntimeException("课程不存在或无权限"));

        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("课程名称不能为空");
        if (capacity == null || capacity <= 0)
            throw new IllegalArgumentException("容量必须大于0");
        if (credit == null || credit <= 0)
            throw new IllegalArgumentException("学分必须大于0");

        course.setName(name.trim());
        course.setDescription(description);
        course.setCredit(credit);
        course.setCapacity(capacity);
        course.setSchedule(schedule);
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id, String currentUsername) {
        User teacher = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Course course = courseRepository.findByIdAndTeacher(id, teacher)
                .orElseThrow(() -> new RuntimeException("课程不存在或无权限"));
        courseRepository.delete(course);
    }

    public List<Course> getTeacherCourses(String username) {
        User teacher = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("教师不存在"));
        return courseRepository.findByTeacher(teacher);
    }

    public Page<Course> searchCourses(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty())
            return courseRepository.findAll(pageable);
        return courseRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
    }
}