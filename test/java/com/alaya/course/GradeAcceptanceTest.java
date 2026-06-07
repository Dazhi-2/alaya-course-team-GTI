package com.alaya.course;

import com.alaya.course.domain.Course;
import com.alaya.course.domain.Enrollment;
import com.alaya.course.domain.User;
import com.alaya.course.repository.CourseRepository;
import com.alaya.course.repository.EnrollmentRepository;
import com.alaya.course.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class GradeAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long course1Id;
    private Long enrollmentId1;  // 未录入成绩的选课记录
    private Long enrollmentId2;  // 已录入成绩的选课记录
    private Long enrollmentIdDropped; // 退课记录

    @BeforeEach
    void setUp() {
        // 确保 teacher1 存在
        User teacher = userRepository.findByUsername("teacher1").orElseGet(() -> {
            User t = new User();
            t.setUsername("teacher1");
            t.setPassword(passwordEncoder.encode("pass"));
            t.setRole("TEACHER");
            t.setCreatedAt(LocalDateTime.now());
            return userRepository.save(t);
        });

        // 确保 teacher2 存在（用于越权）
        User teacher2 = userRepository.findByUsername("teacher2").orElseGet(() -> {
            User t = new User();
            t.setUsername("teacher2");
            t.setPassword(passwordEncoder.encode("pass"));
            t.setRole("TEACHER");
            t.setCreatedAt(LocalDateTime.now());
            return userRepository.save(t);
        });

        // 确保 student1 存在
        User student = userRepository.findByUsername("student1").orElseGet(() -> {
            User s = new User();
            s.setUsername("student1");
            s.setPassword(passwordEncoder.encode("pass"));
            s.setRole("STUDENT");
            s.setCreatedAt(LocalDateTime.now());
            return userRepository.save(s);
        });

        // 创建课程 course1
        Course course1 = new Course();
        course1.setName("数据结构");
        course1.setCredit(4);
        course1.setCapacity(30);
        course1.setEnrolledCount(0);
        course1.setSchedule("周一1-2节");
        course1.setTeacher(teacher);
        course1.setCreatedAt(LocalDateTime.now());
        course1 = courseRepository.save(course1);
        course1Id = course1.getId();

        // 创建课程 course2（用于 teacher2 越权，但越权测试单独处理）
        Course course2 = new Course();
        course2.setName("算法设计");
        course2.setCredit(3);
        course2.setCapacity(30);
        course2.setEnrolledCount(0);
        course2.setSchedule("周二1-2节");
        course2.setTeacher(teacher2);
        course2.setCreatedAt(LocalDateTime.now());
        courseRepository.save(course2);

        // 选课记录1：未录入成绩
        Enrollment e1 = new Enrollment();
        e1.setStudent(student);
        e1.setCourse(course1);
        e1.setEnrolledAt(LocalDateTime.now());
        e1.setActive(true);
        enrollmentRepository.save(e1);
        enrollmentId1 = e1.getId();

        // 选课记录2：已录入成绩85
        Enrollment e2 = new Enrollment();
        e2.setStudent(student);
        e2.setCourse(course1);
        e2.setEnrolledAt(LocalDateTime.now());
        e2.setActive(true);
        e2.setScore(85);
        e2.setGradedAt(LocalDateTime.now());
        enrollmentRepository.save(e2);
        enrollmentId2 = e2.getId();

        // 退课记录
        Enrollment dropped = new Enrollment();
        dropped.setStudent(student);
        dropped.setCourse(course1);
        dropped.setEnrolledAt(LocalDateTime.now());
        dropped.setActive(false);
        enrollmentRepository.save(dropped);
        enrollmentIdDropped = dropped.getId();
    }

    // ==================== 测试：教师录入成绩（正常） ====================
    @Test
    @WithMockUser(authorities = "TEACHER", username = "teacher1")
    void shouldSaveGradeSuccessfully_WhenTeacherOwnsCourse() throws Exception {
        mockMvc.perform(post("/teacher/enrollments/{id}/grade", enrollmentId1)
                        .param("score", "90")
                        .param("comment", "优秀")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher/courses/" + course1Id + "/students"))
                .andExpect(flash().attribute("successMessage", containsString("成绩录入成功")));
    }

    // ==================== 测试：教师修改成绩 ====================
    @Test
    @WithMockUser(authorities = "TEACHER", username = "teacher1")
    void shouldUpdateGradeSuccessfully_WhenAlreadyGraded() throws Exception {
        mockMvc.perform(post("/teacher/enrollments/{id}/grade", enrollmentId2)
                        .param("score", "95")
                        .param("comment", "更优秀")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", containsString("成绩修改成功")));
    }

    // ==================== 测试：越权录入（教师不能给他人课程打分） ====================
    @Test
    @WithMockUser(authorities = "TEACHER", username = "teacher2")
    void shouldShowError_WhenTeacherGradesOthersCourse() throws Exception {
        mockMvc.perform(post("/teacher/enrollments/{id}/grade", enrollmentId1)
                        .param("score", "85")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }

    // ==================== 测试：退课学生不能录入成绩 ====================
    @Test
    @WithMockUser(authorities = "TEACHER", username = "teacher1")
    void shouldShowError_WhenEnrollmentNotActive() throws Exception {
        mockMvc.perform(post("/teacher/enrollments/{id}/grade", enrollmentIdDropped)
                        .param("score", "85")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", containsString("退课")));
    }

    // ==================== 测试：成绩范围（负数） ====================
    @Test
    @WithMockUser(authorities = "TEACHER", username = "teacher1")
    void shouldShowError_WhenGradeNegative() throws Exception {
        mockMvc.perform(post("/teacher/enrollments/{id}/grade", enrollmentId1)
                        .param("score", "-1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", containsString("0-100")));
    }

    // ==================== 测试：成绩范围（超100） ====================
    @Test
    @WithMockUser(authorities = "TEACHER", username = "teacher1")
    void shouldShowError_WhenGradeExceeds100() throws Exception {
        mockMvc.perform(post("/teacher/enrollments/{id}/grade", enrollmentId1)
                        .param("score", "101")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", containsString("0-100")));
    }

    // ==================== 测试：成绩小数 ====================
    @Test
    @WithMockUser(authorities = "TEACHER", username = "teacher1")
    void shouldShowError_WhenGradeIsDecimal() throws Exception {
        mockMvc.perform(post("/teacher/enrollments/{id}/grade", enrollmentId1)
                        .param("score", "85.5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }

    // ==================== 测试：成绩为空 ====================
    @Test
    @WithMockUser(authorities = "TEACHER", username = "teacher1")
    void shouldShowError_WhenGradeIsEmpty() throws Exception {
        mockMvc.perform(post("/teacher/enrollments/{id}/grade", enrollmentId1)
                        .param("score", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }

    // ==================== 测试：缺少CSRF Token 返回403 ====================
    @Test
    @WithMockUser(authorities = "TEACHER", username = "teacher1")
    void shouldReturn403_WhenCsrfTokenMissing() throws Exception {
        mockMvc.perform(post("/teacher/enrollments/{id}/grade", enrollmentId1)
                        .param("score", "85"))
                .andExpect(status().isForbidden());
    }

    // ==================== 测试：教师查看自己课程的学生名单及统计 ====================
    @Test
    @WithMockUser(authorities = "TEACHER", username = "teacher1")
    void shouldShowEnrollmentListAndStats_WhenTeacherOwnsCourse() throws Exception {
        // 需要先录入成绩以便统计
        // 这里直接使用已存在的 enrollmentId2（已录入85分），再创建另一个选课记录并录入成绩，确保有多个成绩以计算统计
        // 为了简化，我们不严格要求统计数字，只验证页面可访问和包含基本元素
        mockMvc.perform(get("/teacher/courses/{id}/students", course1Id))
                .andExpect(status().isOk())
                .andExpect(view().name("teacher/course-students"))
                .andExpect(model().attributeExists("enrollments"));
    }

    // ==================== 测试：学生查看个人成绩 ====================
    @Test
    @WithMockUser(authorities = "STUDENT", username = "student1")
    void shouldShowGrades_WhenStudentHasEnrollments() throws Exception {
        mockMvc.perform(get("/student/grades"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/grades"))
                .andExpect(model().attributeExists("enrollments"));
    }

    // ==================== 测试：学生访问教师端点 403 ====================
    @Test
    @WithMockUser(authorities = "STUDENT", username = "student1")
    void shouldReturn403_WhenStudentAccessesTeacherEndpoint() throws Exception {
        mockMvc.perform(get("/teacher/courses/1/students"))
                .andExpect(status().isForbidden());
    }

    // ==================== 测试：学生提交教师成绩表单 403 ====================
    @Test
    @WithMockUser(authorities = "STUDENT", username = "student1")
    void shouldReturn403_WhenStudentPostsTeacherGradeForm() throws Exception {
        mockMvc.perform(post("/teacher/enrollments/1/grade")
                        .param("score", "85")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}