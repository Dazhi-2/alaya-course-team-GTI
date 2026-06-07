# 迭代4 步骤5：全面交叉验证与重构记录

## 一、跨模型全面审核（Claude-3.5-Sonnet）

**审核发现**：
- 缺少成绩小数校验（前端可能传入85.5导致截断）
- Service 异常类型不统一
- 成绩统计空列表时仍放入统计属性（已修复）
- Thymeleaf 模板中部分 `th:utext` 有 XSS 风险

**人工决策**：
- 采纳：增加小数校验、改用 `th:text`、空列表时不放入统计属性
- 暂不采纳：统一异常类型（当前够用）

## 二、重构执行
- 在 Controller 中添加 `Integer.parseInt(scoreStr)` 捕获 `NumberFormatException`
- 修改 `addGradeStatistics` 仅在 `!graded.isEmpty()` 时添加统计属性
- 模板中所有 `th:utext` 改为 `th:text`

## 三、失败回退记录
无失败回退。

**决策人**：刘书恒