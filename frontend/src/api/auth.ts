import request from './request'
import type { ApiResult, LoginParams, RegisterParams, LoginResponse, UserInfo } from '@/types'

/** 用户注册 */
export function register(data: RegisterParams) {
  return request.post<any, ApiResult<LoginResponse>>('/auth/register', data)
}

/** 用户登录 */
export function login(data: LoginParams) {
  return request.post<any, ApiResult<LoginResponse>>('/auth/login', data)
}

/** 获取当前用户信息 */
export function getCurrentUser() {
  return request.get<any, ApiResult<UserInfo>>('/auth/me')
}
