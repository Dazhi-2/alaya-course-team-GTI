package com.alaya.course;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CourseManagementAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "TEACHER", username = "teacher1")
    void shouldCreateCourse_WhenValidInput() throws Exception {
        mockMvc.perform(post("/teacher/courses")
                .param("name", "数据结构")
                .param("description", "计算机基础")
                .param("credit", "4")
                .param("capacity", "30")
                .param("schedule", "周一1-2节")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher/courses"));
    }

    @Test
    @WithMockUser(authorities = "TEACHER", username = "teacher1")
    void shouldFail_WhenCapacityZero() throws Exception {
        mockMvc.perform(post("/teacher/courses")
                .param("name", "测试")
                .param("credit", "2")
                .param("capacity", "0")
                .param("schedule", "周一1-2节")   // ✅ 添加了这一行
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("course-form"))
                .andExpect(model().attributeExists("error"));
    }

    // 其他测试方法保持不变...
    @Test
    @WithMockUser(authorities = "STUDENT", username = "student1")
    void shouldShowCourseList_WhenStudentLoggedIn() throws Exception {
        mockMvc.perform(get("/student/courses?page=0"))
                .andExpect(status().isOk())
                .andExpect(view().name("course-list"));
    }

    @Test
    @WithMockUser(authorities = "STUDENT", username = "student1")
    void shouldSearch_WhenKeywordMatches() throws Exception {
        mockMvc.perform(get("/student/courses?keyword=数据"))
                .andExpect(status().isOk())
                .andExpect(view().name("course-list"));
    }

    @Test
    @WithMockUser(authorities = "STUDENT", username = "student1")
    void shouldShowAllCourses_WhenKeywordIsSpaces() throws Exception {
        mockMvc.perform(get("/student/courses?keyword=   "))
                .andExpect(status().isOk())
                .andExpect(view().name("course-list"));
    }

    @Test
    @WithMockUser(authorities = "STUDENT", username = "student1")
    void shouldShowDetail_WhenCourseExists() throws Exception {
        mockMvc.perform(get("/student/courses/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRedirectToLogin_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/student/courses"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(authorities = "STUDENT", username = "student1")
    void shouldReturn403_WhenStudentAccessTeacherPage() throws Exception {
        mockMvc.perform(get("/teacher/courses/create"))
                .andExpect(status().isForbidden());
    }
}