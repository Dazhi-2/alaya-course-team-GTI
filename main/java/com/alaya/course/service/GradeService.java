package com.alaya.course.service;

import com.alaya.course.domain.Course;
import com.alaya.course.domain.Enrollment;
import com.alaya.course.domain.User;
import com.alaya.course.repository.CourseRepository;
import com.alaya.course.repository.EnrollmentRepository;
import com.alaya.course.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GradeService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Enrollment saveOrUpdateGrade(Long enrollmentId, Integer score, String comment, String teacherUsername) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("选课记录不存在"));

        if (!enrollment.isActive()) {
            throw new RuntimeException("该学生已退课，无法录入成绩");
        }

        if (!enrollment.getCourse().getTeacher().getUsername().equals(teacherUsername)) {
            throw new RuntimeException("无权限");
        }

        if (score == null || score < 0 || score > 100) {
            throw new RuntimeException("成绩必须在0-100之间");
        }

        enrollment.setScore(score);
        enrollment.setGradeComment(comment);
        enrollment.setGradedAt(LocalDateTime.now());
        return enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> getEnrollmentsByCourse(Long courseId, String teacherUsername) {
        User teacher = userRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Course course = courseRepository.findByIdAndTeacher(courseId, teacher)
                .orElseThrow(() -> new RuntimeException("课程不存在或无权限"));
        return enrollmentRepository.findByCourseOrderByStudentUsernameAsc(course);//为无法成绩录入的学生显示选课记录，方便教师查看和操作原为return enrollmentRepository.findByCourseAndActiveTrueOrderByStudentUsernameAsc(course);
    }

    public List<Enrollment> getEnrollmentsByStudent(String studentUsername) {
        User student = userRepository.findByUsername(studentUsername)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return enrollmentRepository.findByStudentAndActiveTrueOrderByEnrolledAtDesc(student);
    }
}