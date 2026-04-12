import { create } from 'zustand'
import type {
  PracticeSession,
  SessionTrackItem,
  SessionChain,
  DifyChatMessage,
} from '@/types'
import {
  createSession,
  getSession,
  getSessionTrack,
  getUserChains,
  deleteChain,
  touchSession,
} from '@/api/session'
import { getConversationMessages } from '@/api/dify'

interface SessionState {
  /** 当前活跃的轨迹链会话 */
  currentSession: PracticeSession | null
  /** 当前链的题目轨迹（含题目详情） */
  sessionTrack: SessionTrackItem[]
  /** 用户所有轨迹链列表 */
  chains: SessionChain[]
  /** 每条链独立的聊天记录（key 为 sessionId） */
  chatMessagesMap: Record<number, DifyChatMessage[]>
  /** 当前链的聊天消息（从 chatMessagesMap 同步） */
  chatMessages: DifyChatMessage[]
  /** 加载状态 */
  loading: boolean
  chainsLoading: boolean
  chatLoading: boolean

  /** 创建或复用会话 */
  startSession: (userId: number, problemSlug: string, ojPlatform?: string) => Promise<PracticeSession | null>
  /** 加载当前链的轨迹 */
  fetchTrack: (sessionId: number) => Promise<void>
  /** 加载所有轨迹链 */
  fetchChains: () => Promise<void>
  /** 切换到指定轨迹链 */
  switchChain: (sessionId: number) => Promise<void>
  /** 删除轨迹链 */
  removeChain: (sessionId: number) => Promise<boolean>
  /** 清除当前会话 */
  clearSession: () => void
  /** 聊天消息操作 */
  addChatMessage: (msg: DifyChatMessage) => void
  updateChatMessage: (id: string, content: string) => void
  setChatLoading: (loading: boolean) => void
}

let msgCounter = 0

/** 从 Dify 拉取历史消息并写入 chatMessagesMap */
async function loadDifyHistory(
  sessionId: number,
  get: () => SessionState,
  set: (partial: Partial<SessionState>) => void,
) {
  const map = get().chatMessagesMap
  if (map[sessionId] && map[sessionId].length > 0) {
    // 已有本地缓存，直接用
    set({ chatMessages: map[sessionId] })
    return
  }
  try {
    const res = await getConversationMessages(sessionId)
    if (res.code === 200 && res.data && res.data.length > 0) {
      const msgs: DifyChatMessage[] = res.data.map((m, idx) => ({
        id: `history-${sessionId}-${idx}`,
        role: m.role as DifyChatMessage['role'],
        content: m.content,
        timestamp: m.timestamp || Date.now(),
      }))
      const newMap = { ...get().chatMessagesMap, [sessionId]: msgs }
      set({
        chatMessagesMap: newMap,
        chatMessages: get().currentSession?.id === sessionId ? msgs : get().chatMessages,
      })
    }
  } catch {
    /* 静默，拉不到也不影响使用 */
  }
}

export const useSessionStore = create<SessionState>((set, get) => ({
  currentSession: null,
  sessionTrack: [],
  chains: [],
  chatMessagesMap: {},
  chatMessages: [],
  loading: false,
  chainsLoading: false,
  chatLoading: false,

  startSession: async (userId, problemSlug, ojPlatform) => {
    set({ loading: true })
    try {
      const res = await createSession({ userId, problemSlug, ojPlatform })
      if (res.code === 200) {
        const session = res.data
        const sid = session.id
        const map = get().chatMessagesMap
        set({
          currentSession: session,
          chatMessages: map[sid] || [],
        })
        // 异步拉取 Dify 历史消息（不阻塞返回）
        loadDifyHistory(sid, get, set)
        return session
      }
      return null
    } finally {
      set({ loading: false })
    }
  },

  fetchTrack: async (sessionId) => {
    const res = await getSessionTrack(sessionId)
    if (res.code === 200) {
      set({ sessionTrack: res.data })
    }
  },

  fetchChains: async () => {
    set({ chainsLoading: true })
    try {
      const res = await getUserChains()
      if (res.code === 200) {
        set({ chains: res.data })
      }
    } finally {
      set({ chainsLoading: false })
    }
  },

  switchChain: async (sessionId) => {
    try {
      // 更新活跃时间
      touchSession(sessionId).catch(() => {})
      const res = await getSession(sessionId)
      if (res.code === 200) {
        const map = get().chatMessagesMap
        set({
          currentSession: res.data,
          sessionTrack: [],
          chatMessages: map[sessionId] || [],
        })
        // 异步拉取 Dify 历史消息
        loadDifyHistory(sessionId, get, set)
        const trackRes = await getSessionTrack(sessionId)
        if (trackRes.code === 200) {
          set({ sessionTrack: trackRes.data })
        }
      }
    } catch {
      /* 静默 */
    }
  },

  removeChain: async (sessionId) => {
    try {
      const res = await deleteChain(sessionId)
      if (res.code === 200) {
        const state = get()
        const newMap = { ...state.chatMessagesMap }
        delete newMap[sessionId]
        const isCurrent = state.currentSession?.id === sessionId
        set({
          chains: state.chains.filter((c) => c.sessionId !== sessionId),
          chatMessagesMap: newMap,
          ...(isCurrent
            ? { currentSession: null, sessionTrack: [], chatMessages: [] }
            : {}),
        })
        return true
      }
      return false
    } catch {
      return false
    }
  },

  clearSession: () =>
    set({ currentSession: null, sessionTrack: [], chains: [], chatMessages: [] }),

  addChatMessage: (msg) => {
    const sid = get().currentSession?.id
    if (!sid) return
    const map = get().chatMessagesMap
    const prev = map[sid] || []
    const updated = [...prev, msg]
    set({
      chatMessagesMap: { ...map, [sid]: updated },
      chatMessages: updated,
    })
  },

  updateChatMessage: (id, content) => {
    const sid = get().currentSession?.id
    if (!sid) return
    const map = get().chatMessagesMap
    const prev = map[sid] || []
    const updated = prev.map((msg) =>
      msg.id === id ? { ...msg, content } : msg,
    )
    set({
      chatMessagesMap: { ...map, [sid]: updated },
      chatMessages: updated,
    })
  },

  setChatLoading: (loading) => set({ chatLoading: loading }),
}))

/** 快捷创建消息对象 */
export function createChatMsg(
  role: DifyChatMessage['role'],
  content: string,
): DifyChatMessage {
  return {
    id: `msg-${++msgCounter}-${Date.now()}`,
    role,
    content,
    timestamp: Date.now(),
  }
}
