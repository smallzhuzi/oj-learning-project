import type { DifyChatResponse, DifyChatRequest, DifyChatMessage, ApiResult } from '@/types'
import request from './request'

/** SSE 流式回调参数 */
export interface SSECallbacks {
  /** 每次收到文本片段时回调 */
  onChunk: (chunk: string) => void
  /** 流结束时回调完整结果 */
  onDone: (response: DifyChatResponse) => void
  /** 出错时回调 */
  onError: (error: string) => void
}

/**
 * 通用 SSE 流式请求
 * 使用 fetch + ReadableStream 读取后端的 Server-Sent Events
 * 正确解析 Spring SseEmitter 的格式：event:name\ndata:content\n\n
 */
async function fetchSSE(url: string, body: DifyChatRequest, callbacks: SSECallbacks) {
  const token = localStorage.getItem('token')

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body),
  })

  if (response.status === 401) {
    localStorage.removeItem('token')
    window.location.href = '/login'
    throw new Error('未登录或登录已过期')
  }

  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || `请求失败 [${response.status}]`)
  }

  const reader = response.body?.getReader()
  if (!reader) {
    throw new Error('浏览器不支持流式读取')
  }

  const decoder = new TextDecoder()
  let buffer = ''
  // 跟踪当前事件的名称（Spring SSE 先发 event: 行，再发 data: 行）
  let currentEvent = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })

    // 按行解析 SSE 事件
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      const trimmed = line.trim()

      // 空行表示一个事件块结束
      if (trimmed === '') {
        currentEvent = ''
        continue
      }

      // 事件名称行
      if (trimmed.startsWith('event:')) {
        currentEvent = trimmed.slice(6).trim()
        continue
      }

      // 数据行
      if (trimmed.startsWith('data:')) {
        const data = trimmed.slice(5)
        handleSSEData(currentEvent, data, callbacks)
      }
    }
  }

  // 处理缓冲区中剩余内容
  if (buffer.trim()) {
    const trimmed = buffer.trim()
    if (trimmed.startsWith('data:')) {
      handleSSEData(currentEvent, trimmed.slice(5), callbacks)
    }
  }
}

/** 根据事件类型处理 SSE 数据 */
function handleSSEData(event: string, data: string, callbacks: SSECallbacks) {
  if (event === 'message') {
    callbacks.onChunk(data)
  } else if (event === 'done') {
    try {
      const parsed = JSON.parse(data) as DifyChatResponse
      callbacks.onDone(parsed)
    } catch {
      callbacks.onError('解析完成事件失败')
    }
  } else if (event === 'error') {
    callbacks.onError(data)
  }
}

/** 推荐下一题（流式） */
export function recommendNextStream(data: DifyChatRequest, callbacks: SSECallbacks) {
  return fetchSSE('/api/dify/recommend-next', data, callbacks)
}

/** 代码分析（流式） */
export function analyzeCodeStream(data: DifyChatRequest, callbacks: SSECallbacks) {
  return fetchSSE('/api/dify/analyze', data, callbacks)
}

/** 渐进式提示（流式） */
export function requestHintStream(data: DifyChatRequest, callbacks: SSECallbacks) {
  return fetchSSE('/api/dify/hint', data, callbacks)
}

/** 主动提问（流式） */
export function sendChatStream(data: DifyChatRequest, callbacks: SSECallbacks) {
  return fetchSSE('/api/dify/chat', data, callbacks)
}

/** 获取会话的 Dify 历史消息 */
export function getConversationMessages(sessionId: number) {
  return request.get<any, ApiResult<{ role: string; content: string; timestamp: number }[]>>(
    '/dify/messages',
    { params: { sessionId } },
  )
}

/**
 * 通用 SSE 流式请求（支持任意 body）
 */
async function fetchSSEGeneric(url: string, body: Record<string, any>, callbacks: SSECallbacks) {
  const token = localStorage.getItem('token')

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body),
  })

  if (response.status === 401) {
    localStorage.removeItem('token')
    window.location.href = '/login'
    throw new Error('未登录或登录已过期')
  }

  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || `请求失败 [${response.status}]`)
  }

  const reader = response.body?.getReader()
  if (!reader) throw new Error('浏览器不支持流式读取')

  const decoder = new TextDecoder()
  let buffer = ''
  let currentEvent = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      const trimmed = line.trim()
      if (trimmed === '') { currentEvent = ''; continue }
      if (trimmed.startsWith('event:')) { currentEvent = trimmed.slice(6).trim(); continue }
      if (trimmed.startsWith('data:')) {
        handleSSEData(currentEvent, trimmed.slice(5), callbacks)
      }
    }
  }
}

/** 智能组题（流式） */
export function smartGenerateStream(data: {
  count?: number
  selfAssessment?: string
  targetGoal?: string
  preference?: string
  timeBudget?: string
  title?: string
  ojPlatform?: string
}, callbacks: SSECallbacks) {
  return fetchSSEGeneric('/api/dify/smart-generate', data, callbacks)
}

/** 赛后分析（流式） */
export function contestAnalysisStream(data: {
  contestTitle: string
  scoringRule: string
  userRank: number
  totalParticipants: number
  problemResults: string
}, callbacks: SSECallbacks) {
  return fetchSSEGeneric('/api/dify/contest-analysis', data, callbacks)
}
