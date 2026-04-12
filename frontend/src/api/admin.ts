import request from './request'
import type { ApiResult, PageResult, UserInfo } from '@/types'

/** 分页查询用户列表 */
export function getUsers(page: number, size: number, keyword?: string) {
  return request.get<any, ApiResult<PageResult<UserInfo>>>('/admin/users', {
    params: { page, size, keyword: keyword || undefined },
  })
}

/** 切换用户状态（启用/禁用） */
export function toggleUserStatus(userId: number) {
  return request.put<any, ApiResult<string>>(`/admin/users/${userId}/toggle-status`)
}

/** 重置用户密码 */
export function resetUserPassword(userId: number) {
  return request.put<any, ApiResult<string>>(`/admin/users/${userId}/reset-password`)
}

/** 删除用户 */
export function deleteUser(userId: number) {
  return request.delete<any, ApiResult<string>>(`/admin/users/${userId}`)
}

/** 添加用户 */
export function createUser(data: { username: string; email: string; password: string; role: string }) {
  return request.post<any, ApiResult<string>>('/admin/users', data)
}
