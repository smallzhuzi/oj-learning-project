import { create } from 'zustand'
import type { UserInfo, LoginParams, RegisterParams } from '@/types'
import { login as apiLogin, register as apiRegister, getCurrentUser } from '@/api/auth'

interface UserState {
  /** 当前用户信息 */
  user: UserInfo | null
  /** JWT Token */
  token: string | null
  /** 加载中 */
  loading: boolean

  /** 登录 */
  login: (params: LoginParams) => Promise<boolean>
  /** 注册 */
  register: (params: RegisterParams) => Promise<boolean>
  /** 登出 */
  logout: () => void
  /** 从 localStorage 恢复登录状态 */
  restoreSession: () => Promise<void>
}

export const useUserStore = create<UserState>((set) => ({
  user: null,
  token: localStorage.getItem('token'),
  loading: false,

  login: async (params) => {
    try {
      const res = await apiLogin(params)
      if (res.code === 200) {
        const { token, userId, username, email, role } = res.data
        localStorage.setItem('token', token)
        set({
          token,
          user: { id: userId, username, email, role },
        })
        return true
      }
      return false
    } catch {
      return false
    }
  },

  register: async (params) => {
    try {
      const res = await apiRegister(params)
      if (res.code === 200) {
        const { token, userId, username, email, role } = res.data
        localStorage.setItem('token', token)
        set({
          token,
          user: { id: userId, username, email, role },
        })
        return true
      }
      return false
    } catch {
      return false
    }
  },

  logout: () => {
    localStorage.removeItem('token')
    set({ user: null, token: null })
  },

  restoreSession: async () => {
    const token = localStorage.getItem('token')
    if (!token) {
      set({ user: null, token: null })
      return
    }
    try {
      const res = await getCurrentUser()
      if (res.code === 200) {
        set({ user: res.data, token })
      } else {
        localStorage.removeItem('token')
        set({ user: null, token: null })
      }
    } catch {
      localStorage.removeItem('token')
      set({ user: null, token: null })
    }
  },
}))
