import request from './request'
import type { ApiResult, Submission, SubmitCodeParams, UserStats } from '@/types'

/** 提交代码 */
export function submitCode(data: SubmitCodeParams) {
  return request.post<any, ApiResult<Submission>>('/submissions', data)
}

/** 轮询判题结果 */
export function pollSubmissionResult(id: number) {
  return request.get<any, ApiResult<Submission>>(`/submissions/${id}/result`)
}

/** 获取提交详情 */
export function getSubmission(id: number) {
  return request.get<any, ApiResult<Submission>>(`/submissions/${id}`)
}

/** 查询当前用户在某题上的所有提交记录（按时间倒序） */
export function getUserProblemSubmissions(slug: string, ojPlatform = 'leetcode') {
  return request.get<any, ApiResult<Submission[]>>(`/submissions/problem/${slug}`, {
    params: { ojPlatform },
  })
}

/** 批量查询当前用户所有已提交题目的状态（slug → "accepted" / "attempted"） */
export function getUserStatusMap() {
  return request.get<any, ApiResult<Record<string, string>>>('/submissions/status-map')
}

/** 获取用户做题统计（按平台、难度、近期趋势） */
export function getUserStats() {
  return request.get<any, ApiResult<UserStats>>('/submissions/stats')
}
