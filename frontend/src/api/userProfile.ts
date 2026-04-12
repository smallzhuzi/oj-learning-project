import request from './request'
import type { ApiResult, UserProfile } from '@/types'

/** 获取当前用户画像 */
export function getUserProfile() {
  return request.get<any, ApiResult<UserProfile>>('/user-profile')
}

/** 更新用户自评水平和目标 */
export function updateUserProfile(data: { skillLevel?: string; targetLevel?: string }) {
  return request.put<any, ApiResult<UserProfile>>('/user-profile', data)
}

/** 触发用户画像自动分析 */
export function analyzeUserProfile() {
  return request.post<any, ApiResult<UserProfile>>('/user-profile/analyze')
}
