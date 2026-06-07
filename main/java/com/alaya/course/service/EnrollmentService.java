package com.alaya.course.service;

import com.alaya.course.domain.Course;
import com.alaya.course.domain.Enrollment;
import com.alaya.course.domain.User;
import com.alaya.course.repository.CourseRepository;
import com.alaya.course.repository.EnrollmentRepository;
import com.alaya.course.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 学生选课
     */
    @Transactional
    public void enroll(Long courseId, String studentUsername) {
        User student = userRepository.findByUsername(studentUsername)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 1. 检查重复选课
        if (enrollmentRepository.existsByStudentAndCourseAndActiveTrue(student, course)) {
            throw new RuntimeException("已选该课程");
        }

        // 2. 检查容量（注意：此判断仅用于快速失败，最终依靠乐观锁）
        if (course.isFull()) {
            throw new RuntimeException("课程已满");
        }

        // 3. 检查时间冲突
        if (hasTimeConflict(student, course)) {
            throw new RuntimeException("与已有课程时间冲突");
        }

        // 4. 更新容量（乐观锁保护）
        course.setEnrolledCount(course.getEnrolledCount() + 1);
        try {
            courseRepository.save(course);  // 乐观锁在此处生效
        } catch (OptimisticLockingFailureException e) {
            // 并发冲突，重新查询最新课程状态
            Course refreshed = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("课程不存在"));
            if (refreshed.isFull()) {
                throw new RuntimeException("课程已满");
            }
            throw new RuntimeException("系统繁忙，请重试");
        }

        // 5. 创建选课记录
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setActive(true);
        enrollmentRepository.save(enrollment);
    }

    /**
     * 学生退课
     */
    @Transactional
    public void drop(Long enrollmentId, String studentUsername) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("选课记录不存在"));

        // 权限校验：只能退自己的课
        if (!enrollment.getStudent().getUsername().equals(studentUsername)) {
            throw new RuntimeException("无权操作");
        }

        if (!enrollment.isActive()) {
            throw new RuntimeException("已退课，无需重复操作");
        }

        // 逻辑删除
        enrollment.setActive(false);
        enrollmentRepository.save(enrollment);

        // 释放容量（乐观锁保护）
        Course course = enrollment.getCourse();
        course.setEnrolledCount(course.getEnrolledCount() - 1);
        try {
            courseRepository.save(course);
        } catch (OptimisticLockingFailureException e) {
            // 并发退课时，重新查询并再次尝试（本示例简化，直接提示重试）
            throw new RuntimeException("系统繁忙，请重试");
        }
    }

    /**
     * 查询学生的有效选课记录（个人课表）
     */
    public List<Enrollment> getStudentSchedule(String studentUsername) {
        User student = userRepository.findByUsername(studentUsername)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return enrollmentRepository.findByStudentAndActiveTrue(student);
    }

    /**
     * 教师查看自己课程的选课名单
     */
    public List<Enrollment> getCourseEnrollments(Long courseId, String teacherUsername) {
        User teacher = userRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Course course = courseRepository.findByIdAndTeacher(courseId, teacher)
                .orElseThrow(() -> new RuntimeException("课程不存在或无权限"));

        return enrollmentRepository.findByCourseAndActiveTrue(course);
    }

    // ---------- 私有辅助方法 ----------
    private boolean hasTimeConflict(User student, Course newCourse) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentAndActiveTrue(student);
        for (Enrollment e : enrollments) {
            Course existing = e.getCourse();
            String existingSchedule = existing.getSchedule();
            String newSchedule = newCourse.getSchedule();
            // 简化冲突检测：相同时间字符串即为冲突（且都非null）
            if (existingSchedule != null && newSchedule != null
                    && existingSchedule.equals(newSchedule)) {
                return true;
            }
        }
        return false;
    }
}