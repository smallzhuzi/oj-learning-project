import request from './request'
import type { ApiResult } from '@/types'

/** 发送验证码 */
export function sendCode(email: string) {
  return request.post<any, ApiResult<string>>('/auth/send-code', { email })
}

/** 重置密码 */
export function resetPassword(data: { email: string; code: string; newPassword: string }) {
  return request.post<any, ApiResult<string>>('/auth/reset-password', data)
}
