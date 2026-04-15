import request from './request'
import type {
  ApiResult,
  PageResult,
  ContestDetail,
  CreateContestParams,
  ContestTeam,
  ContestTeamLobby,
  ContestTeamDetail,
  MyTeamSummary,
  ContestSubmission,
  StandingData,
  ProblemSetItemDetail,
} from '@/types'

/** 创建比赛 */
export function createContest(data: CreateContestParams) {
  return request.post<any, ApiResult<any>>('/contests', data)
}

/** 比赛列表 */
export function getContests(filter = 'all', pageNum = 1, pageSize = 20, keyword?: string, status?: string) {
  return request.get<any, ApiResult<PageResult<ContestDetail>>>('/contests', {
    params: { filter, pageNum, pageSize, keyword: keyword || undefined, status: status || undefined },
  })
}

/** 比赛详情 */
export function getContestDetail(id: number) {
  return request.get<any, ApiResult<ContestDetail>>(`/contests/${id}`)
}

/** 更新比赛（草稿态） */
export function updateContest(id: number, data: CreateContestParams) {
  return request.put<any, ApiResult<void>>(`/contests/${id}`, data)
}

/** 发布比赛 */
export function publishContest(id: number) {
  return request.post<any, ApiResult<void>>(`/contests/${id}/publish`)
}

/** 报名比赛 */
export function registerContest(id: number, password?: string, teamId?: number, memberUserIds?: number[]) {
  return request.post<any, ApiResult<void>>(`/contests/${id}/register`, memberUserIds ?? [], {
    params: { password: password || undefined, teamId: teamId || undefined },
    headers: { 'Content-Type': 'application/json' },
  })
}

/** 取消报名 */
export function cancelRegistration(id: number) {
  return request.delete<any, ApiResult<void>>(`/contests/${id}/register`)
}

/** 创建队伍 */
export function createTeam(contestId: number, teamName: string, description?: string) {
  return request.post<any, ApiResult<ContestTeam>>(`/contests/${contestId}/teams`, { teamName, description })
}

/** 退出队伍 */
export function leaveTeam(contestId: number, teamId: number) {
  return request.delete<any, ApiResult<void>>(`/contests/${contestId}/teams/${teamId}/leave`)
}

/** 获取队伍列表 */
export function getTeams(contestId: number) {
  return request.get<any, ApiResult<ContestTeamLobby[]>>(`/contests/${contestId}/teams`)
}

export function getMyTeam(contestId: number) {
  return request.get<any, ApiResult<ContestTeamDetail | null>>(`/contests/${contestId}/teams/my`)
}

export function getMyTeams() {
  return request.get<any, ApiResult<MyTeamSummary[]>>('/contests/teams/my')
}

export function updateTeam(contestId: number, teamId: number, teamName: string, description?: string) {
  return request.put<any, ApiResult<ContestTeamDetail>>(`/contests/${contestId}/teams/${teamId}`, { teamName, description })
}

export function transferCaptain(contestId: number, teamId: number, targetUserId: number) {
  return request.post<any, ApiResult<void>>(`/contests/${contestId}/teams/${teamId}/transfer-captain`, { targetUserId })
}

export function removeTeamMember(contestId: number, teamId: number, targetUserId: number) {
  return request.delete<any, ApiResult<void>>(`/contests/${contestId}/teams/${teamId}/members/${targetUserId}`)
}

export function dissolveTeam(contestId: number, teamId: number) {
  return request.delete<any, ApiResult<void>>(`/contests/${contestId}/teams/${teamId}`)
}

/** 比赛提交代码 */
export function contestSubmit(contestId: number, data: { problemSlug: string; language: string; code: string; ojPlatform?: string }) {
  return request.post<any, ApiResult<ContestSubmission>>(`/contests/${contestId}/submit`, data)
}

/** 轮询比赛提交结果 */
export function pollContestResult(contestId: number, submissionId: number) {
  return request.get<any, ApiResult<ContestSubmission>>(`/contests/${contestId}/submissions/${submissionId}/result`)
}

/** 我的比赛提交记录 */
export function getMyContestSubmissions(contestId: number) {
  return request.get<any, ApiResult<ContestSubmission[]>>(`/contests/${contestId}/submissions`)
}

/** 获取榜单 */
export function getStandings(contestId: number) {
  return request.get<any, ApiResult<StandingData>>(`/contests/${contestId}/standings`)
}

/** 解封榜单 */
export function unfreezeStandings(contestId: number) {
  return request.post<any, ApiResult<void>>(`/contests/${contestId}/unfreeze`)
}

/** 获取比赛题目列表 */
export function getContestProblems(contestId: number) {
  return request.get<any, ApiResult<ProblemSetItemDetail[]>>(`/contests/${contestId}/problems`)
}
