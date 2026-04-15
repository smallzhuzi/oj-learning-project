import request from './request'
import type {
  ApiResult,
  PageResult,
  IndTeamListItem,
  IndTeamDetail,
  TeamJoinRequestItem,
} from '@/types'

/** 创建队伍 */
export function createIndTeam(data: { teamName: string; description?: string }) {
  return request.post<any, ApiResult<any>>('/teams', data)
}

/** 队伍广场（分页） */
export function listIndTeams(pageNum = 1, pageSize = 20, keyword?: string) {
  return request.get<any, ApiResult<PageResult<IndTeamListItem>>>('/teams', {
    params: { pageNum, pageSize, keyword: keyword || undefined },
  })
}

/** 我的队伍 */
export function getMyIndTeams() {
  return request.get<any, ApiResult<IndTeamListItem[]>>('/teams/my')
}

/** 我作为队长的队伍 */
export function getMyCaptainTeams() {
  return request.get<any, ApiResult<IndTeamListItem[]>>('/teams/my-captain')
}

/** 队伍详情 */
export function getIndTeamDetail(id: number) {
  return request.get<any, ApiResult<IndTeamDetail>>(`/teams/${id}`)
}

/** 队伍成员列表（用于报名选人） */
export function getIndTeamMembers(id: number) {
  return request.get<any, ApiResult<IndTeamDetail>>(`/teams/${id}`)
}

/** 更新队伍 */
export function updateIndTeam(id: number, data: { teamName?: string; description?: string }) {
  return request.put<any, ApiResult<void>>(`/teams/${id}`, data)
}

/** 解散队伍 */
export function dissolveIndTeam(id: number) {
  return request.delete<any, ApiResult<void>>(`/teams/${id}`)
}

/** 申请加入 */
export function applyToJoinTeam(id: number, message?: string) {
  return request.post<any, ApiResult<void>>(`/teams/${id}/apply`, null, { params: { message: message || undefined } })
}

/** 批准申请 */
export function approveJoinRequest(teamId: number, requestId: number) {
  return request.post<any, ApiResult<void>>(`/teams/${teamId}/requests/${requestId}/approve`)
}

/** 拒绝申请 */
export function rejectJoinRequest(teamId: number, requestId: number) {
  return request.post<any, ApiResult<void>>(`/teams/${teamId}/requests/${requestId}/reject`)
}

/** 待审批列表 */
export function getPendingRequests(teamId: number) {
  return request.get<any, ApiResult<TeamJoinRequestItem[]>>(`/teams/${teamId}/requests`)
}

/** 退出队伍 */
export function leaveIndTeam(id: number) {
  return request.post<any, ApiResult<void>>(`/teams/${id}/leave`)
}

/** 移除成员 */
export function removeIndTeamMember(teamId: number, targetUserId: number) {
  return request.delete<any, ApiResult<void>>(`/teams/${teamId}/members/${targetUserId}`)
}

/** 转让队长 */
export function transferIndTeamCaptain(teamId: number, targetUserId: number) {
  return request.post<any, ApiResult<void>>(`/teams/${teamId}/transfer`, null, { params: { targetUserId } })
}
