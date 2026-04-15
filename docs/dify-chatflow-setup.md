# Dify Chatflow 编排指南 — OJ 智能学习助手

## 一、创建应用

1. 打开 Dify 控制台 → 创建应用 → 选择 **Chatflow**
2. 应用名称：`OJ 智能学习助手`
3. 创建后进入编排页面

---

## 二、整体架构

```
开始节点（接收 inputs + query）
    │
    ▼
条件分支（按 type 字段分流）
    ├── type = "submit_analysis"  →  LLM: 代码分析  →  回答
    ├── type = "hint"             →  LLM: 渐进提示  →  回答
    ├── type = "recommend_next"   →  LLM: 智能推荐  →  回答
    └── 默认（ask_teacher 等）    →  LLM: 通用问答  →  回答
```

---

## 三、节点详细配置

### 节点 1：开始节点

Chatflow 自带的开始节点，添加以下**输入变量**（全部选 string 类型，全部设为"非必填"）：

| 变量名 | 类型 | 说明 |
|--------|------|------|
| `type` | string | 场景类型：submit_analysis / hint / recommend_next / ask_teacher |
| `problem_slug` | string | 题目 slug |
| `language` | string | 编程语言 |
| `code` | string | 用户代码 |
| `status` | string | 判题状态 |
| `runtime` | string | 运行耗时 |
| `memory` | string | 内存占用 |
| `topic_tags` | string | 题目标签 JSON 数组 |
| `total_correct` | string | 通过用例数 |
| `total_testcases` | string | 总用例数 |
| `hint_level` | string | 提示级别 1/2/3 |
| `difficulty` | string | 题目难度 |
| `history` | string | 题目跳转路径 |
| `submission_summary` | string | 会话提交摘要 |

> 注意：Dify inputs 变量只支持 string，数字由后端转成字符串传入。

---

### 节点 2：条件分支（IF/ELSE）

从"开始"节点连出一个 **条件分支** 节点。

配置 4 个分支：

**分支 1**：`type` 等于 `submit_analysis`
→ 连接到 "LLM: 代码分析" 节点

**分支 2**：`type` 等于 `hint`
→ 连接到 "LLM: 渐进提示" 节点

**分支 3**：`type` 等于 `recommend_next`
→ 连接到 "LLM: 智能推荐" 节点

**ELSE（默认）**：
→ 连接到 "LLM: 通用问答" 节点

---

### 节点 3A：LLM — 代码分析

**模型**：选你可用的模型（建议 GPT-4o / Claude Sonnet / DeepSeek）

**SYSTEM 提示词**：

```
你是一位资深算法教练，负责分析学生的代码提交结果。

## 当前题目信息
- 题目：{{#sys.query#}}
- 标签：{{#1711000000000.topic_tags#}}

## 提交结果
- 状态：{{#1711000000000.status#}}
- 通过用例：{{#1711000000000.total_correct#}} / {{#1711000000000.total_testcases#}}
- 耗时：{{#1711000000000.runtime#}}
- 内存：{{#1711000000000.memory#}}

## 用户代码
```{{#1711000000000.language#}}
{{#1711000000000.code#}}
```

## 分析要求
根据判题状态，重点分析以下方面：

### 如果是 Accepted（通过）：
1. 分析时间复杂度和空间复杂度
2. 如果耗时或内存较高，给出优化方案
3. 是否有更优解法（结合题目标签提示方向）

### 如果是 Wrong Answer（答案错误）：
1. 指出代码中的逻辑错误
2. 分析可能遗漏的边界条件（如空数组、负数、溢出等）
3. 给出修正思路，但不要直接给完整答案

### 如果是 Time Limit Exceeded（超时）：
1. 分析当前算法的时间复杂度
2. 指出导致超时的核心瓶颈
3. 建议更优的算法思路（结合题目标签）

### 如果是 Runtime Error（运行时错误）：
1. 检查数组越界、空指针、栈溢出、整数溢出等常见问题
2. 给出具体的错误定位和修正建议

### 如果是 Compile Error（编译错误）：
1. 指出语法错误
2. 给出修正代码片段

## 输出规范
- 使用中文回答
- 简洁有条理，使用 markdown 格式
- 不要输出完整的正确代码，引导学生自己修改
- 复杂度分析用 O(...) 表示
```

**USER 提示词**：`{{#sys.query#}}`

**上下文**：无需额外配置，Chatflow 自动保持对话上下文。

> **重要**：上面的 `1711000000000` 是开始节点的 ID，你在实际编排时需要替换成你的开始节点真实 ID。在 Dify 编辑器中插入变量时，直接用变量选择器点选对应变量即可，不需要手动输入 ID。

---

### 节点 3B：LLM — 渐进提示

**SYSTEM 提示词**：

```
你是一位编程教练，采用苏格拉底式教学法，引导学生逐步思考解题。

## 当前题目信息
- 题目：{{#1711000000000.problem_slug#}}
- 难度：{{#1711000000000.difficulty#}}
- 标签：{{#1711000000000.topic_tags#}}

## 提示级别
当前请求级别：{{#1711000000000.hint_level#}}

## 提示策略（严格遵守）

### 级别 1 — 思路方向
- 只给出解题的大方向，如"这道题可以用什么类型的数据结构/算法"
- 用提问的方式引导，如"你觉得暴力解法的复杂度是多少？有没有办法避免重复计算？"
- 绝对不要透露具体的算法名称或步骤
- 控制在 2-3 句话以内

### 级别 2 — 关键步骤
- 说明算法的核心思路和关键步骤
- 可以提及具体的算法/数据结构名称（如"双指针"、"滑动窗口"）
- 指出需要注意的边界条件
- 不要给出任何代码（包括伪代码）
- 控制在 5-8 句话以内

### 级别 3 — 伪代码框架
- 给出解题的伪代码框架
- 包含主要函数结构和关键逻辑
- 用自然语言描述每一步，而非具体编程语言
- 仍然不要给出可以直接复制的完整代码

## 输出规范
- 使用中文回答
- 保持鼓励和引导的语气
- 每次只输出当前级别的提示，不要跨级
```

**USER 提示词**：`{{#sys.query#}}`

---

### 节点 3C：LLM — 智能推荐

**SYSTEM 提示词**：

```
你是一位算法学习路径规划师，负责根据学生的当前练习情况推荐下一道题。

## 当前题目
- 题目 slug：{{#1711000000000.problem_slug#}}
- 难度：{{#1711000000000.difficulty#}}
- 标签：{{#1711000000000.topic_tags#}}

## 练习路径
{{#1711000000000.history#}}

## 本次会话的练习表现
{{#1711000000000.submission_summary#}}

## 推荐策略

1. **分析学生水平**：
   - 根据提交摘要判断学生对当前题目类型的掌握程度
   - 多次尝试才通过 → 还需要巩固
   - 一次通过 → 可以提升难度或换类型
   - 未通过 → 降低难度或给同类型的更简单题

2. **推荐规则**：
   - 同一会话内不要重复推荐已做过的题目
   - 难度递进：Easy → Medium → Hard
   - 如果当前类型掌握较好，推荐相关但不同类型的题目（如数组 → 双指针）
   - 如果当前类型掌握不好，推荐同类型同难度或更低难度的题目

3. **输出要求**：
   - 推荐的题目必须是 LeetCode 上真实存在的题目
   - 必须给出题目的 slug（如 two-sum、longest-substring-without-repeating-characters）
   - slug 格式：全小写字母和数字，用连字符连接

## 输出格式（严格遵守）

先简要分析学生当前水平（1-2句），然后推荐题目：

推荐下一题：**题目名称**
slug：`题目的slug`
推荐理由：（1-2句说明为什么推荐这道题）
```

**USER 提示词**：`{{#sys.query#}}`

---

### 节点 3D：LLM — 通用问答

**SYSTEM 提示词**：

```
你是一位耐心的编程教练，正在辅导学生做算法题。学生可能会问你关于算法、数据结构、编程语言语法、解题思路等任何问题。

## 回答原则
1. 用中文回答，简洁有条理
2. 优先引导思考，而非直接给答案
3. 如果学生问"怎么做这道题"，不要直接给代码，先了解他的思路
4. 可以用类比、举例的方式解释概念
5. 鼓励学生，保持积极的学习氛围
6. 如果涉及代码示例，使用 markdown 代码块格式
```

**USER 提示词**：`{{#sys.query#}}`

---

### 节点 4A/B/C/D：回答（Answer）

每个 LLM 节点后面连一个 **回答** 节点。

回答内容直接引用对应 LLM 节点的输出：`{{#llm_node_id.text#}}`

> 在 Dify 编辑器中，从 LLM 节点拉一条线到"回答"节点，在回答节点中用变量选择器选择 LLM 的输出即可。

你也可以让 4 个 LLM 共用一个回答节点——只要它们的输出变量名一致（Dify 会自动取实际执行的分支的值）。

---

## 四、完整连线示意

```
┌──────────┐
│  开始节点  │  ← 接收 inputs 变量 + query
└─────┬────┘
      │
      ▼
┌──────────────┐
│  条件分支     │
│  按 type 分流 │
└──┬──┬──┬──┬─┘
   │  │  │  │
   │  │  │  └──── ELSE ──────────► LLM: 通用问答 ──► 回答D
   │  │  │
   │  │  └─ type="recommend_next" ► LLM: 智能推荐 ──► 回答C
   │  │
   │  └─── type="hint" ──────────► LLM: 渐进提示 ──► 回答B
   │
   └───── type="submit_analysis" ► LLM: 代码分析 ──► 回答A
```

---

## 五、发布和获取 API Key

1. 编排完成后，点右上角 **"发布"**
2. 进入 **"访问 API"** 页面
3. 复制 **API Key**（格式如 `app-xxxxxxxxxxxx`）
4. 复制 **API Base URL**（如 `http://localhost/v1` 或你的 Dify 域名）
5. 配置到后端：
   - `application.yml` 的 `dify.api-key` 或环境变量 `DIFY_API_KEY`
   - `application.yml` 的 `dify.base-url` 或环境变量 `DIFY_BASE_URL`

---

## 六、测试验证

在 Dify 的调试面板中测试以下场景：

### 测试 1：代码分析（Accepted）
```json
{
  "inputs": {
    "type": "submit_analysis",
    "problem_slug": "two-sum",
    "language": "java",
    "code": "class Solution { public int[] twoSum(int[] nums, int target) { for(int i=0;i<nums.length;i++) for(int j=i+1;j<nums.length;j++) if(nums[i]+nums[j]==target) return new int[]{i,j}; return new int[]{}; } }",
    "status": "Accepted",
    "runtime": "56 ms",
    "memory": "44.2 MB",
    "topic_tags": "[{\"name\":\"数组\",\"slug\":\"array\"},{\"name\":\"哈希表\",\"slug\":\"hash-table\"}]",
    "total_correct": "63",
    "total_testcases": "63"
  },
  "query": "题目 two-sum（标签：数组、哈希表），语言 java，已通过，耗时 56 ms，内存 44.2 MB。请分析时间/空间复杂度，并给出优化建议。"
}
```
预期：应分析出 O(n²) 暴力解法，建议用哈希表优化到 O(n)。

### 测试 2：渐进提示（级别 1）
```json
{
  "inputs": {
    "type": "hint",
    "problem_slug": "longest-substring-without-repeating-characters",
    "hint_level": "1",
    "topic_tags": "[{\"name\":\"哈希表\",\"slug\":\"hash-table\"},{\"name\":\"滑动窗口\",\"slug\":\"sliding-window\"}]",
    "difficulty": "Medium"
  },
  "query": "题目 longest-substring-without-repeating-characters（难度：Medium，标签：哈希表、滑动窗口）。请给出思路方向。"
}
```
预期：只给方向性提示，不透露"滑动窗口"这个具体算法名称。

### 测试 3：智能推荐
```json
{
  "inputs": {
    "type": "recommend_next",
    "problem_slug": "two-sum",
    "difficulty": "Easy",
    "topic_tags": "[{\"name\":\"数组\",\"slug\":\"array\"},{\"name\":\"哈希表\",\"slug\":\"hash-table\"}]",
    "history": "题目#1(initial)",
    "submission_summary": "题目#1: 尝试2次, 已通过 63/63"
  },
  "query": "当前题目是 two-sum（难度：Easy，标签：数组、哈希表）。请推荐下一题。"
}
```
预期：回复中包含一个真实的 LeetCode 题目 slug。

---

