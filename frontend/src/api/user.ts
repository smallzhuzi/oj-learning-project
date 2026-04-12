import request from './request'
import type { ApiResult, UserInfo, UpdateProfileParams, ChangePasswordParams, UserOjConfig, SaveOjConfigParams } from '@/types'

/** 获取用户信息 */
export function getUserInfo() {
  return request.get<any, ApiResult<UserInfo>>('/user/info')
}

/** 修改个人资料 */
export function updateProfile(data: UpdateProfileParams) {
  return request.put<any, ApiResult<string>>('/user/profile', data)
}

/** 修改密码 */
export function changePassword(data: ChangePasswordParams) {
  return request.put<any, ApiResult<string>>('/user/password', data)
}

/** 获取用户所有 OJ 配置 */
export function getOjConfigs() {
  return request.get<any, ApiResult<UserOjConfig[]>>('/user/oj-configs')
}

/** 保存或更新 OJ 配置 */
export function saveOjConfig(data: SaveOjConfigParams) {
  return request.put<any, ApiResult<string>>('/user/oj-config', data)
}
