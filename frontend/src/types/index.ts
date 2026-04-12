/** 题目实体 */
export interface Problem {
  id: number
  slug: string
  title: string
  difficulty: string
  acceptanceRate: number | null
  ojPlatform: string
  contentMarkdown: string | null
  codeSnippets: CodeSnippet[] | null
  topicTags: { name: string; slug: string }[] | null
  frontendId: string | null
  questionId: string | null
  createdAt: string
  updatedAt: string
}

/** 代码模板（对应 LeetCode codeSnippets） */
export interface CodeSnippet {
  lang: string
  langSlug: string
  code: string
}

/** 提交记录实体 */
export interface Submission {
  id: number
  userId: number
  problemId: number
  sessionId: number | null
  language: string
  code: string
  status: string
  runtime: string | null
  memory: string | null
  totalCorrect: number | null
  totalTestcases: number | null
  remoteSubmissionId: string | null
  submittedAt: string
}

/** 练习会话实体 */
export interface PracticeSession {
  id: number
  userId: number
  difyConversationId: string | null
  startedAt: string
  endedAt: string | null
}

/** 会话题目关联 */
export interface SessionProblem {
  id: number
  sessionId: number
  problemId: number
  jumpType: 'initial' | 'next_recommend'
  seqOrder: number
  jumpedAt: string
}

/** 会话轨迹项（含题目详情，用于侧边栏展示） */
export interface SessionTrackItem {
  id: number
  sessionId: number
  problemId: number
  jumpType: 'initial' | 'next_recommend'
  seqOrder: number
  jumpedAt: string
  slug: string
  title: string
  frontendId: string
  difficulty: string
  /** 该题在该会话内的提交次数 */
  attemptCount: number
  /** 该题在该会话内是否已通过 */
  accepted: boolean
}

/** 轨迹链概要（侧边栏链列表） */
export interface SessionChain {
  sessionId: number
  difyConversationId: string | null
  startedAt: string
  headSlug: string
  headTitle: string
  headFrontendId: string
  headDifficulty: string
  problemCount: number
}

/** 统一 API 响应格式 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/** 提交代码请求参数 */
export interface SubmitCodeParams {
  problemSlug: string
  language: string
  code: string
  userId: number
  sessionId?: number
  ojPlatform?: string
}

/** 创建会话请求参数 */
export interface CreateSessionParams {
  userId: number
  problemSlug: string
  ojPlatform?: string
}

/** 题目查询参数 */
export interface ProblemQueryParams {
  keyword?: string
  difficulty?: string
  ojPlatform?: string
  pageNum?: number
  pageSize?: number
}

// ==================== 用户认证相关类型 ====================

/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  email: string
  role?: string
  status?: string
}

/** 登录请求参数 */
export interface LoginParams {
  username: string
  password: string
}

/** 注册请求参数 */
export interface RegisterParams {
  username: string
  email: string
  password: string
  code: string
}

/** 登录/注册响应数据 */
export interface LoginResponse {
  token: string
  userId: number
  username: string
  email: string
  role: string
}

// ==================== Dify 相关类型 ====================

/** Dify 聊天消息（前端展示用） */
export interface DifyChatMessage {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  timestamp: number
}

/** Dify API 响应 */
export interface DifyChatResponse {
  messageId: string | null
  conversationId: string | null
  answer: string
  nextProblemSlug: string | null
  createdAt: number
}

/** Dify 聊天请求参数 */
export interface DifyChatRequest {
  sessionId: number
  message?: string
  problemSlug?: string
  language?: string
  code?: string
  judgeStatus?: string
  runtime?: string
  memory?: string
  totalCorrect?: number
  totalTestcases?: number
  hintLevel?: number
  ojPlatform?: string
}

// ==================== 用户做题统计 ====================

export interface StatSummary {
  solved: number
  submitted: number
  acceptanceRate: number
}

export interface DailyCount {
  date: string
  count: number
}

export interface UserStats {
  total: StatSummary
  platforms: Record<string, StatSummary>
  difficulties: Record<string, Record<string, number>>
  recentDaily: DailyCount[]
}

// ==================== 代码草稿相关类型 ====================

/** 代码草稿 */
export interface CodeDraft {
  id: number
  userId: number
  problemSlug: string
  language: string
  code: string
  createdAt: string
  updatedAt: string
}

/** 保存草稿请求参数 */
export interface SaveDraftParams {
  problemSlug: string
  language: string
  code: string
}

// ==================== 用户个人中心相关类型 ====================

/** 修改个人资料参数 */
export interface UpdateProfileParams {
  email?: string
  code?: string
}

/** 修改密码参数 */
export interface ChangePasswordParams {
  oldPassword: string
  newPassword: string
}

/** 用户 OJ 平台配置 */
export interface UserOjConfig {
  id: number
  userId: number
  ojPlatform: string
  cookieValue: string | null
  csrfToken: string | null
  extraConfig: string | null
  createdAt: string
  updatedAt: string
}

/** 保存 OJ 配置参数 */
export interface SaveOjConfigParams {
  ojPlatform: string
  cookieValue: string
  csrfToken: string
  extraConfig?: string
}

// ==================== 题单相关类型 ====================

/** 题单实体 */
export interface ProblemSet {
  id: number
  userId: number
  title: string
  description: string | null
  sourceType: 'manual' | 'quick' | 'dify_smart'
  difficultyLevel: string | null
  problemCount: number
  totalScore: number
  tags: string[] | null
  difyParams: Record<string, any> | null
  visibility: 'private' | 'public' | 'contest_only'
  status: 'draft' | 'published' | 'archived'
  ojPlatform: string
  createdAt: string
  updatedAt: string
}

/** 题单题目详情 */
export interface ProblemSetItemDetail {
  id: number
  setId: number
  problemId: number
  seqOrder: number
  score: number
  slug: string
  title: string
  frontendId: string
  difficulty: string
  acceptanceRate: number | null
  topicTags: string | null
}

/** 创建题单参数 */
export interface CreateProblemSetParams {
  title: string
  description?: string
  difficultyLevel?: string
  ojPlatform?: string
  problems?: { slug: string; score?: number }[]
}

/** 快速组题参数 */
export interface QuickGenerateParams {
  title?: string
  count: number
  difficultyLevel: string
  distribution?: { easy: number; medium: number; hard: number }
  tags?: string[]
  excludeSolved?: boolean
  ojPlatform?: string
}

/** Dify 智能组题参数 */
export interface SmartGenerateParams {
  title?: string
  count: number
  selfAssessment: string
  targetGoal: string
  preference?: string
  timeBudget?: string
  ojPlatform?: string
}

// ==================== 用户画像相关类型 ====================

/** 用户画像 */
export interface UserProfile {
  id: number
  userId: number
  skillLevel: string
  targetLevel: string | null
  strongTags: string[] | null
  weakTags: string[] | null
  solvedEasy: number
  solvedMedium: number
  solvedHard: number
  totalSubmissions: number
  acceptanceRate: number
  lastAnalyzedAt: string | null
  createdAt: string
  updatedAt: string
}

// ==================== 比赛相关类型 ====================

/** 比赛详情 */
export interface ContestDetail {
  id: number
  creatorName: string
  title: string
  description: string | null
  contestType: 'individual' | 'team'
  status: 'draft' | 'registering' | 'running' | 'frozen' | 'ended' | 'archived'
  startTime: string
  endTime: string
  durationMinutes: number
  freezeMinutes: number
  scoringRule: 'acm' | 'oi' | 'cf'
  penaltyTime: number
  maxParticipants: number
  maxTeamSize: number
  isPublic: boolean
  ojPlatform: string
  registeredCount: number
  teamCount: number
  problemCount: number
  registered: boolean
  isCreator: boolean
  createdAt: string
}

/** 创建比赛参数 */
export interface CreateContestParams {
  title: string
  description?: string
  contestType: 'individual' | 'team'
  startTime: string
  durationMinutes: number
  freezeMinutes?: number
  maxParticipants?: number
  maxTeamSize?: number
  scoringRule?: string
  penaltyTime?: number
  allowLanguage?: string[]
  isPublic?: boolean
  password?: string
  ojPlatform?: string
  problemSource?: 'manual' | 'existing_set' | 'auto'
  problemSetId?: number
  problems?: { slug: string; score?: number }[]
}

/** 比赛队伍 */
export interface ContestTeam {
  id: number
  contestId: number
  teamName: string
  captainId: number
  inviteCode: string
  memberCount: number
  createdAt: string
}

/** 比赛提交 */
export interface ContestSubmission {
  id: number
  contestId: number
  userId: number
  teamId: number | null
  problemId: number
  language: string
  code: string
  status: string
  runtime: string | null
  memory: string | null
  totalCorrect: number | null
  totalTestcases: number | null
  score: number
  remoteSubmissionId: string | null
  submittedAt: string
}

/** 榜单数据 */
export interface StandingData {
  contestId: number
  scoringRule: string
  frozen: boolean
  problems: StandingProblem[]
  rows: StandingRow[]
}

export interface StandingProblem {
  problemId: number
  slug: string
  title: string
  frontendId: string
  score: number
}

export interface StandingRow {
  rank: number
  userId: number | null
  username: string | null
  teamId: number | null
  teamName: string | null
  solvedCount: number
  totalScore: number
  totalPenalty: number
  problemResults: ProblemResult[]
}

export interface ProblemResult {
  problemId: number
  accepted: boolean
  attempts: number
  firstAcTimeSeconds: number | null
  score: number
  frozen: boolean
}
