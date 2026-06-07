package com.alaya.course;

import com.alaya.course.domain.Course;
import com.alaya.course.domain.Enrollment;
import com.alaya.course.domain.User;
import com.alaya.course.repository.CourseRepository;
import com.alaya.course.repository.EnrollmentRepository;
import com.alaya.course.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@SpringBootApplication
public class CourseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      CourseRepository courseRepository,
                                      EnrollmentRepository enrollmentRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // === 创建教师 teacher1（如果不存在） ===
            User teacher1 = userRepository.findByUsername("teacher1").orElseGet(() -> {
                User t = new User();
                t.setUsername("teacher1");
                t.setPassword(passwordEncoder.encode("teacher123"));
                t.setRole("TEACHER");
                t.setCreatedAt(LocalDateTime.now());
                return userRepository.save(t);
            });

            // === 创建教师 teacher2（用于越权测试） ===
            User teacher2 = userRepository.findByUsername("teacher2").orElseGet(() -> {
                User t = new User();
                t.setUsername("teacher2");
                t.setPassword(passwordEncoder.encode("teacher123"));
                t.setRole("TEACHER");
                t.setCreatedAt(LocalDateTime.now());
                return userRepository.save(t);
            });

            // === 创建学生 student1 ===
            User student1 = userRepository.findByUsername("student1").orElseGet(() -> {
                User s = new User();
                s.setUsername("student1");
                s.setPassword(passwordEncoder.encode("student123"));
                s.setRole("STUDENT");
                s.setCreatedAt(LocalDateTime.now());
                return userRepository.save(s);
            });

            // === 创建学生 student2（可选，用于其他测试） ===
            User student2 = userRepository.findByUsername("student2").orElseGet(() -> {
                User s = new User();
                s.setUsername("student2");
                s.setPassword(passwordEncoder.encode("student123"));
                s.setRole("STUDENT");
                s.setCreatedAt(LocalDateTime.now());
                return userRepository.save(s);
            });

            // === 初始化课程（仅当 courses 表为空时） ===
            Course course1 = null;
            if (courseRepository.count() == 0) {
                Course c1 = new Course();
                c1.setName("数据结构");
                c1.setDescription("基础课程");
                c1.setCredit(4);
                c1.setCapacity(30);
                c1.setEnrolledCount(0);
                c1.setSchedule("周一1-2节");
                c1.setTeacher(teacher1);
                c1.setCreatedAt(LocalDateTime.now());
                course1 = courseRepository.save(c1);

                Course c2 = new Course();
                c2.setName("算法设计");
                c2.setDescription("高阶算法");
                c2.setCredit(3);
                c2.setCapacity(1);
                c2.setEnrolledCount(1);
                c2.setSchedule("周二1-2节");
                c2.setTeacher(teacher1);
                c2.setCreatedAt(LocalDateTime.now());
                courseRepository.save(c2);

                Course c3 = new Course();
                c3.setName("操作系统");
                c3.setDescription("核心课程");
                c3.setCredit(4);
                c3.setCapacity(30);
                c3.setEnrolledCount(0);
                c3.setSchedule("周一1-2节");
                c3.setTeacher(teacher1);
                c3.setCreatedAt(LocalDateTime.now());
                courseRepository.save(c3);
                
                System.out.println("✅ 预置课程已创建");
            } else {
                // 如果课程已存在，尝试获取“数据结构”课程（用于选课记录）
                course1 = courseRepository.findAll().stream()
                        .filter(c -> "数据结构".equals(c.getName()))
                        .findFirst()
                        .orElse(null);
            }

            // === 创建选课记录（仅当 enrollments 表为空时） ===
            if (enrollmentRepository.count() == 0 && course1 != null) {
                // 正常选课记录（active=true，成绩 null）
                Enrollment e1 = new Enrollment();
                e1.setStudent(student1);
                e1.setCourse(course1);
                e1.setEnrolledAt(LocalDateTime.now());
                e1.setActive(true);
                e1.setScore(null);
                enrollmentRepository.save(e1);
                
                // 为方便演示成绩统计，再添加两个学生选课并预设成绩
                // 注意：这里需要确保 student2 存在
                Enrollment e2 = new Enrollment();
                e2.setStudent(student2);
                e2.setCourse(course1);
                e2.setEnrolledAt(LocalDateTime.now());
                e2.setActive(true);
                e2.setScore(80);
                e2.setGradedAt(LocalDateTime.now());
                enrollmentRepository.save(e2);
                
                // 第三个学生（如果没有第三个学生，可以复用 student1 再建一条不同课程？但为了统计演示，最好有三个不同学生）
                // 简单起见，再创建一个学生 student3
                User student3 = userRepository.findByUsername("student3").orElseGet(() -> {
                    User s = new User();
                    s.setUsername("student3");
                    s.setPassword(passwordEncoder.encode("student123"));
                    s.setRole("STUDENT");
                    s.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(s);
                });
                Enrollment e3 = new Enrollment();
                e3.setStudent(student3);
                e3.setCourse(course1);
                e3.setEnrolledAt(LocalDateTime.now());
                e3.setActive(true);
                e3.setScore(100);
                e3.setGradedAt(LocalDateTime.now());
                enrollmentRepository.save(e3);
                
                // 创建一条退课记录（active=false）用于“退课学生录入”异常测试
                Enrollment dropped = new Enrollment();
                dropped.setStudent(student1);
                dropped.setCourse(course1);
                dropped.setEnrolledAt(LocalDateTime.now());
                dropped.setActive(false);
                enrollmentRepository.save(dropped);
                
                System.out.println("✅ 预置选课记录已创建（含正常、已评分、退课记录）");
            }
        };
    }
}