package com.alaya.course.repository;

import com.alaya.course.domain.Course;
import com.alaya.course.domain.Enrollment;
import com.alaya.course.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 迭代3已有
    List<Enrollment> findByStudentAndActiveTrue(User student);
    List<Enrollment> findByCourseAndActiveTrue(Course course);
    boolean existsByStudentAndCourseAndActiveTrue(User student, Course course);
    Optional<Enrollment> findById(Long id);

    // 迭代4需要（成绩管理排序查询）
    List<Enrollment> findByCourseAndActiveTrueOrderByStudentUsernameAsc(Course course);
    List<Enrollment> findByStudentAndActiveTrueOrderByEnrolledAtDesc(User student);
    
    // 新增：查询课程下所有 enrollment（不区分 active），用于教师看到退课学生
    List<Enrollment> findByCourseOrderByStudentUsernameAsc(Course course);
}