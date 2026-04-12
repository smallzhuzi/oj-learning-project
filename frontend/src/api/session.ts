import request from './request'
import type {
  ApiResult,
  PracticeSession,
  SessionProblem,
  SessionTrackItem,
  SessionChain,
  CreateSessionParams,
} from '@/types'

/** 创建或复用练习会话（find-or-create） */
export function createSession(data: CreateSessionParams) {
  return request.post<any, ApiResult<PracticeSession>>('/sessions', data)
}

/** 获取会话详情 */
export function getSession(id: number) {
  return request.get<any, ApiResult<PracticeSession>>(`/sessions/${id}`)
}

/** 获取会话题目轨迹（含题目详情） */
export function getSessionTrack(id: number) {
  return request.get<any, ApiResult<SessionTrackItem[]>>(`/sessions/${id}/track`)
}

/** 在会话中添加下一题（AI 推荐） */
export function addNextProblem(sessionId: number, problemSlug: string, ojPlatform = 'leetcode') {
  return request.post<any, ApiResult<SessionProblem>>(
    `/sessions/${sessionId}/next`,
    null,
    { params: { problemSlug, ojPlatform } },
  )
}

/** 绑定 Dify 对话 ID */
export function bindDifyConversation(sessionId: number, conversationId: string) {
  return request.put<any, ApiResult<void>>(
    `/sessions/${sessionId}/dify`,
    null,
    { params: { conversationId } },
  )
}

/** 获取当前用户所有轨迹链 */
export function getUserChains() {
  return request.get<any, ApiResult<SessionChain[]>>('/sessions/chains')
}

/** 删除轨迹链 */
export function deleteChain(sessionId: number) {
  return request.delete<any, ApiResult<void>>(`/sessions/${sessionId}`)
}

/** 触发会话活跃（更新排序） */
export function touchSession(sessionId: number) {
  return request.put<any, ApiResult<void>>(`/sessions/${sessionId}/touch`)
}
