# 迭代4 功能分解与 GWT 验收标准（服务端渲染版·简化版设计）

## 接口契约（锁定）

- 教师录入/修改成绩：POST `/teacher/enrollments/{id}/grade`，表单字段 `score`、`comment`，成功重定向到 `/teacher/courses/{courseId}/students`，失败返回原页面 + `model.addAttribute("error", ...)`
- 教师查看课程学生名单及成绩统计：GET `/teacher/courses/{id}/students`，返回视图名 `teacher/course-students`，Model 属性 `enrollments` (List\<EnrollmentGradeDTO\>)、`averageScore`、`maxScore`、`minScore`、`gradedCount`
- 学生查看个人成绩：GET `/student/grades`，返回视图名 `student/grades`，Model 属性 `enrollments` (List\<EnrollmentGradeDTO\>)
- 错误属性名：`error`
- 成功提示属性名：`successMessage`
- 统计属性名：`averageScore`（保留1位小数）、`maxScore`、`minScore`、`gradedCount`

---

## US-1：教师录入/修改成绩（简化版：直接修改 Enrollment.score 字段）

**业务规则**：
- 只有教师角色可以操作。
- 只能给自己的课程的学生打分。
- 只能给 `active=true` 的 Enrollment 录入成绩。
- 成绩范围 0-100 整数，null 表示未录入。
- 支持修改（直接更新 score 字段）。

**验收测试标准（GWT）**：
1. **正常-首次录入**：Given 教师 teacher1 登录，课程 C1 有学生 student1（Enrollment ID=1，active=true，score=null），When 提交 POST `/teacher/enrollments/1/grade?score=85&comment=良好`，Then Enrollment.score=85，gradedAt 更新，重定向到 `/teacher/courses/1/students`，提示“成绩录入成功”。
2. **正常-修改成绩**：Given 同一 Enrollment 已有 score=85，When 提交 score=90，Then score 更新为 90，gradedAt 更新，提示“成绩修改成功”。
3. **异常-越权**：Given 教师 teacher2 登录，When 提交 teacher1 课程的 Enrollment 成绩，Then 提示“无权限”，score 未变更。
4. **异常-退课学生**：Given Enrollment.active=false，When 录入成绩，Then 提示“该学生已退课，无法录入成绩”。
5. **异常-非法成绩**：When 提交 score=-1，Then 提示“成绩必须在0-100之间”；score=101 同理；score=abc 返回格式错误。
6. **边界-0分**：Given 提交 score=0，Then 录入成功。
7. **边界-100分**：Given 提交 score=100，Then 录入成功。
8. **边界-小数**：Given 提交 score=85.5，Then 提示“成绩必须为整数”。
9. **边界-空值**：Given 提交 score=""，Then 提示“成绩不能为空”。

## US-2：教师查看课程学生名单及成绩统计

**业务规则**：
- 只能查看自己课程的学生名单。
- 显示所有 `active=true` 的学生，含已录入/未录入状态。
- 统计区域显示：已录入人数、平均分（保留1位小数）、最高分、最低分。
- 无已录入成绩时，统计区域显示“暂无已录入成绩”（不放入统计属性）。

**验收测试标准**：
1. **正常-3人统计**：Given 课程有 3 名学生，成绩分别为 80、90、100，When 教师查看名单，Then 列表显示 3 条记录，统计区域显示 `gradedCount=3`、`averageScore=90.0`、`maxScore=100`、`minScore=80`。
2. **边界-全部未录入**：Given 课程有学生但全部 score=null，When 查看名单，Then 列表显示记录，统计区域显示“暂无已录入成绩”（不显示 `averageScore` 等）。
3. **边界-只有1人**：Given 只有 1 名学生且 score=85，Then 统计区域显示 `gradedCount=1`、`averageScore=85.0`、`maxScore=85`、`minScore=85`。
4. **边界-无学生**：Given 课程无 Enrollment 记录，Then 显示空列表，统计区域不显示。
5. **异常-越权**：Given 教师 teacher2 查看 teacher1 的课程，Then 返回 403 或错误提示。

## US-3：学生查看个人成绩

**业务规则**：
- 只显示自己的 `active=true` 的选课记录。
- 显示课程名称、成绩、状态（已录入/未录入）、评语。

**验收测试标准**：
1. **正常**：Given 学生已选 2 门课程，1 门已录入 85 分，1 门未录入，When 访问 `/student/grades`，Then 显示 2 条记录，已录入显示 85 分及状态“已录入”，未录入显示“---”及状态“未录入”。
2. **边界-无选课**：Given 学生无任何 Enrollment，Then 显示“暂无选课记录”。
3. **异常-越权**：Given 学生 student1 尝试通过篡改 URL 查看 student2 的成绩，Then 只能看到自己的记录（Service 层过滤）。

---

## 跨模型AI复审报告（步骤2）

**生成AI**：DeepSeek-V3  
**复审AI**：Kimi-k1.5  
**复审时间**：2026-06-04  

### 通过项 ✅
- 覆盖成绩录入/修改、成绩统计、学生成绩查看，含正常/异常/边界。
- 接口契约明确，统计属性名清晰。
- 明确采用简化版设计（Enrollment 字段）。

### 不通过项 ❌
- 【高风险】US-1 缺少“成绩为小数”的边界场景 → 已补充为 GWT 1.⑧。
- 【中风险】US-2 缺少“全部未录入”的统计边界 → 已补充为 GWT 2.②。
- 【中风险】US-2 缺少“课程无学生”的边界 → 已补充为 GWT 2.④。
- 【低风险】缺少 CSRF 相关测试场景 → 将在步骤3测试中明确要求。

### 人工决策
- 采纳复审AI的所有补充建议。
- 接口契约已锁定。
- 生成AI：DeepSeek-V3，复审AI：Kimi-k1.5，人工确认人：刘书恒。

## 接口对齐确认表
| 本迭代约定接口 | 迭代3实际接口 | 是否一致 | 处理方式 |
|---------------|--------------|----------|----------|
| GET /student/schedule | GET /student/schedule | ✅ | 一致 |
| GET /teacher/courses/{id}/students | GET /teacher/courses/{id}/students | ✅ | 一致 |
| 角色前缀（无ROLE_） | 无ROLE_前缀 | ✅ | 一致 |
| Enrollment 实体有 active 字段 | 有 active 字段 | ✅ | 一致 |
| 本迭代采用简化版（Enrollment.score） | 确认采用 | ✅ | 是 |