# 迭代4 前置检查清单

## 检查项1：角色前缀一致性
- SecurityConfig 使用 `hasAuthority("STUDENT")`，无 `ROLE_` 前缀 ✅
- 数据库 `users` 表中 `role` 字段为 `STUDENT` / `TEACHER` ✅
- 测试注解使用 `@WithMockUser(authorities = "STUDENT")` ✅

## 检查项2：data.sql 预置数据完整性（改用代码初始化）
- 教师 `teacher1` / 学生 `student1` 已通过 CommandLineRunner 初始化 ✅
- 至少3门课程，包含 `active=true` 的 enrollment 记录 ✅

## 检查项3：迭代3接口路径与视图名对齐
- 学生课表：`GET /student/schedule` → 视图 `student/schedule` ✅
- 教师查看名单：`GET /teacher/courses/{id}/students` → 视图 `teacher/course-students` ✅

## 检查项4：Enrollment 实体状态
- 已包含 `active` 字段 ✅
- 本迭代已追加 `score`, `gradeComment`, `gradedAt` 字段 ✅

**检查人**：刘书恒  
**检查日期**：2026-06-05  
**结果**：✅ 全部通过，可开始迭代4