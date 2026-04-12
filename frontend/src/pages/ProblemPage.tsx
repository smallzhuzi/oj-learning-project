import { useEffect, useState, useRef, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Editor from '@monaco-editor/react'
import Markdown from 'react-markdown'
import remarkMath from 'remark-math'
import rehypeKatex from 'rehype-katex'
import 'katex/dist/katex.min.css'
import {
  PanelLeftOpen, SkipForward, Send, RotateCcw, Lightbulb,
  Loader2, MessageSquare, Bot, User, ArrowLeft,
  ChevronDown, ChevronRight, ChevronUp, CheckCircle2, XCircle,
} from 'lucide-react'
import { useProblemStore } from '@/store/problemStore'
import { useSessionStore, createChatMsg } from '@/store/sessionStore'
import { useUserStore } from '@/store/userStore'
import { submitCode, pollSubmissionResult, getUserProblemSubmissions } from '@/api/submission'
import { recommendNextStream, analyzeCodeStream, sendChatStream, requestHintStream } from '@/api/dify'
import { addNextProblem } from '@/api/session'
import { saveDraft, getDrafts } from '@/api/draft'
import { PLATFORM_LANGUAGES, DEFAULT_CODE_TEMPLATES, PLATFORM_LABELS } from '@/config/platforms'
import UserMenu from '@/components/UserMenu'
import TrackSidebar from '@/components/TrackSidebar'
import type { Submission, DifyChatResponse } from '@/types'

/** 提交状态颜色 */
const statusColor: Record<string, string> = {
  Accepted: 'bg-emerald-900/40 text-emerald-400',
  'Wrong Answer': 'bg-red-900/40 text-red-400',
  'Time Limit Exceeded': 'bg-amber-900/40 text-amber-400',
  'Memory Limit Exceeded': 'bg-amber-900/40 text-amber-400',
  'Runtime Error': 'bg-orange-900/40 text-orange-400',
  'Compile Error': 'bg-orange-900/40 text-orange-400',
  'Output Limit Exceeded': 'bg-orange-900/40 text-orange-400',
}

export default function ProblemPage() {
  const { slug } = useParams<{ slug: string }>()
  const navigate = useNavigate()

  const { currentProblem, fetchProblemDetail, loading: problemLoading, ojPlatform } = useProblemStore()
  const {
    currentSession, sessionTrack, chatMessages, chatLoading, chains, chainsLoading,
    startSession, fetchTrack, fetchChains, switchChain, removeChain,
    addChatMessage, updateChatMessage, setChatLoading,
  } = useSessionStore()
  const { user } = useUserStore()

  /** 根据当前平台动态获取语言列表 */
  const LANGUAGES = PLATFORM_LANGUAGES[ojPlatform] || PLATFORM_LANGUAGES.leetcode

  const [historyOpen, setHistoryOpen] = useState(true)
  const [language, setLanguage] = useState(LANGUAGES[0])
  const [code, setCode] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [lastResult, setLastResult] = useState<Submission | null>(null)
  const [chatInput, setChatInput] = useState('')
  const [recommending, setRecommending] = useState(false)

  // 渐进式提示：每道题的提示级别 { [slug]: currentLevel }
  const [hintLevels, setHintLevels] = useState<Record<string, number>>({})
  const [hintLoading, setHintLoading] = useState(false)

  // 提交记录列表
  const [problemSubmissions, setProblemSubmissions] = useState<Submission[]>([])
  const [submissionsOpen, setSubmissionsOpen] = useState(false)

  // 代码模板和草稿的映射 { langSlug: code }
  const [snippetsMap, setSnippetsMap] = useState<Record<string, string>>({})
  const [draftsMap, setDraftsMap] = useState<Record<string, string>>({})
  const [draftsLoaded, setDraftsLoaded] = useState(false)
  const [dataReady, setDataReady] = useState(false)

  // 可拖拽面板尺寸
  const [rightPanelWidth, setRightPanelWidth] = useState(480)
  const [chatHeight, setChatHeight] = useState(224)
  const [chatCollapsed, setChatCollapsed] = useState(false)
  const [dragDirection, setDragDirection] = useState<'horizontal' | 'vertical' | null>(null)

  // Refs：用于 setInterval/beforeunload 中访问最新值
  const codeRef = useRef('')
  const sessionInitRef = useRef<string | null>(null)
  /** 从"下一题"或侧边栏跳转时跳过创建新会话 */
  const skipSessionInitRef = useRef(false)
  const languageRef = useRef(LANGUAGES[0].value)
  const lastSavedRef = useRef('')

  const pollTimerRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const localSaveTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const savedChatHeightRef = useRef(224)
  const chatEndRef = useRef<HTMLDivElement>(null)
  const mainContainerRef = useRef<HTMLDivElement>(null)
  const rightPanelRef = useRef<HTMLDivElement>(null)

  /** 根据优先级获取某语言的代码：草稿 > localStorage > 模板 > 平台默认模板 > 空 */
  const resolveCode = useCallback((langSlug: string) => {
    if (draftsMap[langSlug]) return draftsMap[langSlug]
    const local = localStorage.getItem(`draft:${slug}:${langSlug}`)
    if (local) return local
    if (snippetsMap[langSlug]) return snippetsMap[langSlug]
    // 平台默认模板兜底（洛谷不提供代码模板）
    return DEFAULT_CODE_TEMPLATES[ojPlatform]?.[langSlug] || ''
  }, [draftsMap, snippetsMap, slug, ojPlatform])

  /** 聊天框自动滚动到底部 */
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [chatMessages])

  /** 初始化：加载题目 + 创建/复用会话 + 加载链列表 */
  useEffect(() => {
    if (!slug || !user) return
    // 重置所有编辑器相关状态，防止旧题目数据残留
    setDataReady(false)
    setDraftsLoaded(false)
    setSnippetsMap({})
    setDraftsMap({})
    setCode('')
    codeRef.current = ''
    lastSavedRef.current = ''
    setLastResult(null)
    fetchProblemDetail(slug, ojPlatform)
    fetchChains()

    if (skipSessionInitRef.current) {
      // 从"下一题"或侧边栏跳转进来，会话已由调用方设置好，无需创建
      skipSessionInitRef.current = false
      sessionInitRef.current = null
    } else if (sessionInitRef.current !== slug) {
      // 从页面1进入：创建或复用会话
      sessionInitRef.current = slug
      startSession(user.id, slug, ojPlatform).then((session) => {
        if (session) fetchTrack(session.id)
      })
    }

    return () => {
      if (pollTimerRef.current != null) clearInterval(pollTimerRef.current)
    }
  }, [slug])

  /** 加载服务端草稿 */
  useEffect(() => {
    if (!slug) return
    setDraftsLoaded(false)
    setDraftsMap({})
    getDrafts(slug).then((res) => {
      if (res.code === 200 && res.data) {
        const map: Record<string, string> = {}
        for (const d of res.data) map[d.language] = d.code
        setDraftsMap(map)
      }
    }).catch(() => { /* 静默 */ }).finally(() => setDraftsLoaded(true))
  }, [slug])

  /** 加载当前题目的提交记录 */
  const loadProblemSubmissions = useCallback(() => {
    if (!slug) return
    getUserProblemSubmissions(slug, ojPlatform).then((res) => {
      if (res.code === 200) setProblemSubmissions(res.data || [])
    }).catch(() => {})
  }, [slug, ojPlatform])

  useEffect(() => {
    setProblemSubmissions([])
    loadProblemSubmissions()
  }, [slug])

  /** 题目 + 草稿都准备好后，构建模板映射并设置编辑器初始代码 */
  useEffect(() => {
    if (dataReady) return
    if (!currentProblem || currentProblem.slug !== slug) return
    if (!draftsLoaded) return

    // 直接从 currentProblem 构建模板映射（避免依赖 snippetsMap state 导致时序问题）
    const sMap: Record<string, string> = {}
    if (currentProblem.codeSnippets) {
      for (const sn of currentProblem.codeSnippets) {
        sMap[sn.langSlug] = sn.code
      }
      setSnippetsMap(sMap)
    }

    // 按优先级获取代码：草稿 > localStorage > 模板 > 空
    const langSlug = language.value
    const resolved = draftsMap[langSlug]
      || localStorage.getItem(`draft:${slug}:${langSlug}`)
      || sMap[langSlug]
      || DEFAULT_CODE_TEMPLATES[ojPlatform]?.[langSlug]
      || ''
    setCode(resolved)
    codeRef.current = resolved
    lastSavedRef.current = resolved
    setDataReady(true)
  }, [currentProblem, draftsMap, draftsLoaded, slug])

  /** 会话创建后加载轨迹 */
  useEffect(() => {
    if (currentSession) {
      fetchTrack(currentSession.id)
    }
  }, [currentSession])

  // ==================== 自动保存 ====================

  /** localStorage debounce 保存（2s） */
  const saveToLocal = useCallback((val: string) => {
    if (localSaveTimerRef.current) clearTimeout(localSaveTimerRef.current)
    localSaveTimerRef.current = setTimeout(() => {
      if (slug) localStorage.setItem(`draft:${slug}:${languageRef.current}`, val)
    }, 2000)
  }, [slug])

  /** 后端周期保存（30s interval） */
  useEffect(() => {
    if (!slug || !user) return
    const timer = setInterval(() => {
      const cur = codeRef.current
      if (cur && cur !== lastSavedRef.current) {
        saveDraft({ problemSlug: slug, language: languageRef.current, code: cur })
          .then(() => { lastSavedRef.current = cur })
          .catch(() => { /* 静默 */ })
      }
    }, 30000)
    return () => clearInterval(timer)
  }, [slug, user])

  /** 页面离开前保存到 localStorage */
  useEffect(() => {
    const handleBeforeUnload = () => {
      if (slug && codeRef.current) {
        localStorage.setItem(`draft:${slug}:${languageRef.current}`, codeRef.current)
      }
    }
    window.addEventListener('beforeunload', handleBeforeUnload)
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload)
      handleBeforeUnload()
    }
  }, [slug])

  // ==================== 编辑器操作 ====================

  /** 代码变更处理 */
  const handleCodeChange = (val: string | undefined) => {
    const v = val || ''
    setCode(v)
    codeRef.current = v
    saveToLocal(v)
  }

  /** 切换编程语言 */
  const handleLanguageChange = (newLangValue: string) => {
    const newLang = LANGUAGES.find((l) => l.value === newLangValue)
    if (!newLang) return

    // 保存当前语言的代码
    if (slug && code) {
      localStorage.setItem(`draft:${slug}:${language.value}`, code)
      setDraftsMap((prev) => ({ ...prev, [language.value]: code }))
    }

    // 加载新语言的代码
    const newCode = resolveCode(newLang.value)
    setLanguage(newLang)
    setCode(newCode)
    codeRef.current = newCode
    languageRef.current = newLang.value
  }

  /** 重置为初始模板 */
  const handleResetCode = () => {
    const template = snippetsMap[language.value]
      || DEFAULT_CODE_TEMPLATES[ojPlatform]?.[language.value]
      || ''
    setCode(template)
    codeRef.current = template
    if (slug) {
      localStorage.removeItem(`draft:${slug}:${language.value}`)
      setDraftsMap((prev) => {
        const next = { ...prev }
        delete next[language.value]
        return next
      })
    }
  }

  // ==================== 面板拖拽 ====================

  /** 水平拖拽：题目描述 ↔ 编辑器+聊天框 */
  const handleHorizontalDragStart = useCallback((e: React.MouseEvent) => {
    e.preventDefault()
    setDragDirection('horizontal')
    const startX = e.clientX
    const startWidth = rightPanelWidth

    const onMouseMove = (ev: MouseEvent) => {
      const rect = mainContainerRef.current?.getBoundingClientRect()
      if (!rect) return
      const delta = startX - ev.clientX
      const sidebarWidth = historyOpen ? 256 : 0
      const available = rect.width - sidebarWidth - 4
      setRightPanelWidth(Math.max(350, Math.min(available - 300, startWidth + delta)))
    }

    const onMouseUp = () => {
      setDragDirection(null)
      document.removeEventListener('mousemove', onMouseMove)
      document.removeEventListener('mouseup', onMouseUp)
    }

    document.addEventListener('mousemove', onMouseMove)
    document.addEventListener('mouseup', onMouseUp)
  }, [rightPanelWidth, historyOpen])

  /** 垂直拖拽：编辑器 ↔ 聊天框 */
  const handleVerticalDragStart = useCallback((e: React.MouseEvent) => {
    e.preventDefault()
    setDragDirection('vertical')
    const startY = e.clientY
    const startHeight = chatHeight

    const onMouseMove = (ev: MouseEvent) => {
      const rect = rightPanelRef.current?.getBoundingClientRect()
      if (!rect) return
      const delta = startY - ev.clientY
      const fixedHeight = 84 // 语言栏 ~36 + 提交按钮栏 ~44 + 手柄 ~4
      const available = rect.height - fixedHeight
      setChatHeight(Math.max(120, Math.min(available - 150, startHeight + delta)))
    }

    const onMouseUp = () => {
      setDragDirection(null)
      document.removeEventListener('mousemove', onMouseMove)
      document.removeEventListener('mouseup', onMouseUp)
    }

    document.addEventListener('mousemove', onMouseMove)
    document.addEventListener('mouseup', onMouseUp)
  }, [chatHeight])

  // ==================== 判题和 Dify ====================

  /** 轮询判题结果 */
  const startPolling = useCallback((submissionId: number) => {
    if (pollTimerRef.current != null) clearInterval(pollTimerRef.current)

    pollTimerRef.current = setInterval(async () => {
      try {
        const res = await pollSubmissionResult(submissionId)
        if (res.code === 200 && res.data.status !== 'Pending') {
          setLastResult(res.data)
          setSubmitting(false)
          if (pollTimerRef.current != null) clearInterval(pollTimerRef.current)

          const s = res.data
          const msg = s.status === 'Accepted'
            ? `通过! 耗时 ${s.runtime}，内存 ${s.memory}`
            : `${s.status} (${s.totalCorrect}/${s.totalTestcases})`
          addChatMessage(createChatMsg('system', `[判题结果] ${msg}`))

          // 刷新提交记录列表和轨迹
          loadProblemSubmissions()
          if (currentSession) fetchTrack(currentSession.id)

          triggerAnalysis(s)
        }
      } catch {
        if (pollTimerRef.current != null) clearInterval(pollTimerRef.current)
        setSubmitting(false)
      }
    }, 1500)
  }, [currentSession, slug, code, language])

  /** 触发 Dify 代码分析（流式） */
  const triggerAnalysis = async (submission: Submission) => {
    if (!currentSession || !slug) return
    setChatLoading(true)

    const placeholderMsg = createChatMsg('assistant', '')
    addChatMessage(placeholderMsg)
    let accumulated = ''

    try {
      await analyzeCodeStream(
        {
          sessionId: currentSession.id,
          problemSlug: slug,
          code: submission.code || code,
          language: submission.language || language.value,
          judgeStatus: submission.status,
          runtime: submission.runtime || undefined,
          memory: submission.memory || undefined,
          totalCorrect: submission.totalCorrect ?? undefined,
          totalTestcases: submission.totalTestcases ?? undefined,
          ojPlatform,
        },
        {
          onChunk: (chunk) => {
            accumulated += chunk
            updateChatMessage(placeholderMsg.id, accumulated)
          },
          onDone: () => {},
          onError: (err) => {
            addChatMessage(createChatMsg('system', `[分析失败] ${err}`))
          },
        },
      )
    } catch (err: any) {
      addChatMessage(createChatMsg('system', `[分析失败] ${err.message}`))
    } finally {
      setChatLoading(false)
    }
  }

  /** 提交代码 */
  const handleSubmit = async () => {
    if (!slug || !currentProblem || submitting) return
    setSubmitting(true)
    setLastResult(null)

    try {
      const res = await submitCode({
        problemSlug: slug,
        language: language.value,
        code,
        userId: user!.id,
        sessionId: currentSession?.id,
        ojPlatform,
      })
      if (res.code === 200) {
        addChatMessage(createChatMsg('system', '代码已提交，等待判题...'))
        startPolling(res.data.id)
      }
    } catch (err: any) {
      addChatMessage(createChatMsg('system', `[提交失败] ${err.message}`))
      setSubmitting(false)
    }
  }

  /** 侧边栏：点击题目跳转（可能需要切换链） */
  const handleTrackNavigate = async (targetSlug: string, sessionId: number) => {
    if (currentSession?.id !== sessionId) {
      await switchChain(sessionId)
      fetchChains()
    }
    skipSessionInitRef.current = true
    navigate(`/problem/${targetSlug}`)
  }

  /** 侧边栏：删除轨迹链 */
  const handleDeleteChain = async (sessionId: number) => {
    const isCurrent = currentSession?.id === sessionId
    const ok = await removeChain(sessionId)
    if (ok) {
      fetchChains()
      if (isCurrent) {
        // 删除当前链后跳转到其他链的头题，或回题库
        const remaining = chains.filter((c) => c.sessionId !== sessionId)
        if (remaining.length > 0) {
          await switchChain(remaining[0].sessionId)
          skipSessionInitRef.current = true
          navigate(`/problem/${remaining[0].headSlug}`)
        } else {
          navigate('/')
        }
      }
    }
  }

  /** 下一题：调用 Dify 推荐（流式） */
  const handleNextProblem = async () => {
    if (!currentSession || !slug || recommending) return
    setRecommending(true)
    addChatMessage(createChatMsg('system', '正在请求 AI 推荐下一题...'))

    const placeholderMsg = createChatMsg('assistant', '')
    addChatMessage(placeholderMsg)
    let accumulated = ''
    let doneResponse = null as DifyChatResponse | null

    try {
      await recommendNextStream(
        {
          sessionId: currentSession.id,
          problemSlug: slug,
          ojPlatform,
        },
        {
          onChunk: (chunk) => {
            accumulated += chunk
            updateChatMessage(placeholderMsg.id, accumulated)
          },
          onDone: (response) => {
            doneResponse = response
          },
          onError: (err) => {
            addChatMessage(createChatMsg('system', `[推荐失败] ${err}`))
          },
        },
      )

      // 流结束后处理导航逻辑
      let nextSlug = doneResponse?.nextProblemSlug || null

      // 兜底：如果后端正则没提取到 slug，在前端从累积文本中尝试提取
      if (!nextSlug && accumulated) {
        // 策略1：结构化标签 [NEXT_SLUG:xxx]
        const tagMatch = accumulated.match(/\[NEXT_SLUG:([a-z0-9]+(?:-[a-z0-9]+)*)\]/i)
        if (tagMatch) {
          nextSlug = tagMatch[1].toLowerCase()
        } else if (ojPlatform === 'luogu') {
          // 策略2（洛谷）：匹配洛谷题号 P1001, B2001, T12345 等
          const luoguMatch = accumulated.match(/\b([PBCTUp]\d{4,5})\b/i)
          if (luoguMatch) nextSlug = luoguMatch[1].toUpperCase()
        } else {
          // 策略2（LeetCode）：LeetCode URL
          const urlMatch = accumulated.match(/leetcode\.cn\/problems\/([a-z0-9]+(?:-[a-z0-9]+)*)/i)
          if (urlMatch) {
            nextSlug = urlMatch[1].toLowerCase()
          } else {
            // 策略3：slug 模式（至少含一个连字符）
            const slugMatch = accumulated.match(/\b([a-z][a-z0-9]*(?:-[a-z0-9]+)+)\b/)
            if (slugMatch) nextSlug = slugMatch[1]
          }
        }
      }

      console.log('[handleNextProblem] 提取结果:', { nextSlug, hasResponse: !!doneResponse, accLen: accumulated.length })

      if (nextSlug) {
        try {
          await addNextProblem(currentSession.id, nextSlug, ojPlatform)
        } catch (navErr: any) {
          // 会话记录失败不阻断导航，仍然跳转到推荐题目
          console.warn('[handleNextProblem] addNextProblem 失败，但仍尝试导航:', navErr.message)
          addChatMessage(createChatMsg('system', `[会话记录异常] ${navErr.message}，仍尝试跳转...`))
        }
        try {
          await fetchTrack(currentSession.id)
        } catch { /* 忽略 track 刷新失败 */ }
        fetchChains()
        skipSessionInitRef.current = true
        navigate(`/problem/${nextSlug}`)
      } else {
        addChatMessage(createChatMsg('system', '未能解析推荐题目，请查看 AI 回复手动选择。'))
      }
    } catch (err: any) {
      addChatMessage(createChatMsg('system', `[推荐失败] ${err.message}`))
    } finally {
      setRecommending(false)
    }
  }

  /** 主动提问（流式） */
  const handleSendChat = async () => {
    if (!currentSession || !chatInput.trim() || chatLoading) return
    const question = chatInput.trim()
    setChatInput('')

    addChatMessage(createChatMsg('user', question))
    setChatLoading(true)

    const placeholderMsg = createChatMsg('assistant', '')
    addChatMessage(placeholderMsg)
    let accumulated = ''

    try {
      await sendChatStream(
        {
          sessionId: currentSession.id,
          message: question,
          ojPlatform,
        },
        {
          onChunk: (chunk) => {
            accumulated += chunk
            updateChatMessage(placeholderMsg.id, accumulated)
          },
          onDone: () => {},
          onError: (err) => {
            addChatMessage(createChatMsg('system', `[回复失败] ${err}`))
          },
        },
      )
    } catch (err: any) {
      addChatMessage(createChatMsg('system', `[回复失败] ${err.message}`))
    } finally {
      setChatLoading(false)
    }
  }

  /** 渐进式提示（流式） */
  const handleRequestHint = async () => {
    if (!currentSession || !slug || hintLoading) return
    const currentLevel = (hintLevels[slug] || 0) + 1
    if (currentLevel > 3) {
      addChatMessage(createChatMsg('system', '已达到最高提示级别'))
      return
    }

    setHintLoading(true)
    const levelLabels = ['', '思路方向', '关键步骤', '伪代码框架']
    addChatMessage(createChatMsg('user', `请求提示（级别${currentLevel}：${levelLabels[currentLevel]}）`))

    const placeholderMsg = createChatMsg('assistant', '')
    addChatMessage(placeholderMsg)
    let accumulated = ''
    let success = false

    try {
      await requestHintStream(
        {
          sessionId: currentSession.id,
          problemSlug: slug,
          hintLevel: currentLevel,
          ojPlatform,
        },
        {
          onChunk: (chunk) => {
            accumulated += chunk
            updateChatMessage(placeholderMsg.id, accumulated)
          },
          onDone: () => {
            success = true
          },
          onError: (err) => {
            addChatMessage(createChatMsg('system', `[提示失败] ${err}`))
          },
        },
      )

      if (success) {
        setHintLevels((prev) => ({ ...prev, [slug!]: currentLevel }))
      }
    } catch (err: any) {
      addChatMessage(createChatMsg('system', `[提示失败] ${err.message}`))
    } finally {
      setHintLoading(false)
    }
  }

  const monacoLang = language.monacoLang

  return (
    <div className="h-screen flex flex-col bg-gray-900 text-gray-100">

      {/* 顶部导航栏 */}
      <header className="flex items-center gap-4 px-4 py-2 bg-gray-800 border-b border-gray-700 shrink-0">
        <button
          onClick={() => navigate('/')}
          className="p-1.5 rounded hover:bg-gray-700 transition"
          title="返回题库"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>

        <div className="w-px h-5 bg-gray-600" />

        {!historyOpen && (
          <button
            onClick={() => setHistoryOpen(true)}
            className="flex items-center gap-1.5 px-2.5 py-1 rounded text-xs text-gray-400 hover:text-gray-200 hover:bg-gray-700 transition"
            title="展开历史面板"
          >
            <PanelLeftOpen className="w-4 h-4" />
            <span>轨迹</span>
          </button>
        )}

        <span className="px-3 py-1 text-xs bg-gray-700 rounded">{PLATFORM_LABELS[ojPlatform] || ojPlatform}</span>

        <span className="text-sm font-medium">
          {currentProblem
            ? `${currentProblem.frontendId}. ${currentProblem.title}`
            : '加载中...'}
        </span>

        <div className="flex-1" />

        <button
          onClick={handleNextProblem}
          disabled={recommending}
          className="flex items-center gap-1.5 px-4 py-1.5 text-sm bg-indigo-600 rounded hover:bg-indigo-700 disabled:opacity-50 transition"
        >
          {recommending ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <SkipForward className="w-3.5 h-3.5" />}
          下一题
        </button>

        {user && (
          <>
            <div className="w-px h-5 bg-gray-600" />
            <UserMenu />
          </>
        )}
      </header>

      {/* 拖拽遮罩层：防止 Monaco iframe 吞掉鼠标事件 */}
      {dragDirection && (
        <div
          className="fixed inset-0 z-50"
          style={{ cursor: dragDirection === 'horizontal' ? 'col-resize' : 'row-resize' }}
        />
      )}

      {/* 主体区域 */}
      <div
        ref={mainContainerRef}
        className={`flex flex-1 overflow-hidden ${dragDirection ? 'select-none' : ''}`}
      >

        {/* 左侧：练习轨迹侧边栏 */}
        <TrackSidebar
          open={historyOpen}
          onClose={() => setHistoryOpen(false)}
          chains={chains}
          chainsLoading={chainsLoading}
          currentSessionId={currentSession?.id ?? null}
          currentSlug={slug}
          sessionTrack={sessionTrack}
          onNavigate={handleTrackNavigate}
          onDeleteChain={handleDeleteChain}
        />

        {/* 中央：题目描述 */}
        <div className="flex-1 overflow-y-auto p-6">
          {problemLoading ? (
            <div className="flex items-center justify-center h-full text-gray-500">
              <Loader2 className="w-6 h-6 animate-spin mr-2" />
              加载题目中...
            </div>
          ) : currentProblem?.contentMarkdown ? (
            ojPlatform === 'luogu' ? (
              <div className="prose prose-invert max-w-none text-sm">
                <Markdown remarkPlugins={[remarkMath]} rehypePlugins={[rehypeKatex]}>{currentProblem.contentMarkdown}</Markdown>
              </div>
            ) : (
              <div
                className="prose prose-invert max-w-none text-sm"
                dangerouslySetInnerHTML={{ __html: currentProblem.contentMarkdown }}
              />
            )
          ) : (
            <p className="text-gray-500 text-sm">暂无题目描述</p>
          )}

          {/* 提交记录面板 */}
          <div className="mt-6 border-t border-gray-700 pt-4">
            <button
              onClick={() => setSubmissionsOpen((v) => !v)}
              className="flex items-center gap-1.5 text-sm text-gray-300 hover:text-gray-100 transition mb-2"
            >
              {submissionsOpen
                ? <ChevronDown className="w-4 h-4" />
                : <ChevronRight className="w-4 h-4" />}
              提交记录
              {problemSubmissions.length > 0 && (
                <span className="text-xs text-gray-500">({problemSubmissions.length})</span>
              )}
            </button>

            {submissionsOpen && (
              problemSubmissions.length === 0 ? (
                <p className="text-xs text-gray-500 py-2">暂无提交记录</p>
              ) : (
                <div className="space-y-1.5 max-h-64 overflow-y-auto">
                  {problemSubmissions.map((sub) => (
                    <div
                      key={sub.id}
                      className="flex items-center gap-3 px-3 py-2 rounded bg-gray-800/60 text-xs"
                    >
                      {/* 状态 */}
                      <span className={`shrink-0 px-1.5 py-0.5 rounded text-[11px] font-medium ${
                        statusColor[sub.status] || 'bg-gray-700 text-gray-400'
                      }`}>
                        {sub.status === 'Accepted' ? 'AC' : sub.status}
                      </span>
                      {/* 语言 */}
                      <span className="text-gray-400 shrink-0">{sub.language}</span>
                      {/* 耗时 + 内存 */}
                      {sub.runtime && <span className="text-gray-500">{sub.runtime}</span>}
                      {sub.memory && <span className="text-gray-500">{sub.memory}</span>}
                      {/* 测试用例 */}
                      {sub.totalCorrect != null && sub.totalTestcases != null && (
                        <span className="text-gray-500">{sub.totalCorrect}/{sub.totalTestcases}</span>
                      )}
                      {/* 时间 */}
                      <span className="text-gray-600 ml-auto shrink-0">
                        {new Date(sub.submittedAt).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                  ))}
                </div>
              )
            )}
          </div>
        </div>

        {/* 水平拖拽手柄 */}
        <div
          className="w-1 shrink-0 bg-gray-700 hover:bg-indigo-500 cursor-col-resize transition-colors"
          onMouseDown={handleHorizontalDragStart}
        />

        {/* 右侧：代码编辑器 + Dify 聊天框 */}
        <div
          ref={rightPanelRef}
          className="flex flex-col shrink-0"
          style={{ width: rightPanelWidth }}
        >

          {/* 编程语言选择 + 提示按钮 + 重置按钮 */}
          <div className="flex items-center justify-end gap-2 px-3 py-1.5 bg-gray-800 border-b border-gray-700">
            <button
              onClick={handleRequestHint}
              disabled={hintLoading || !currentSession || (slug ? (hintLevels[slug] || 0) >= 3 : true)}
              className="flex items-center gap-1 px-2 py-1 text-xs text-amber-400 hover:text-amber-300 hover:bg-gray-700 rounded transition disabled:opacity-40 disabled:cursor-not-allowed"
              title={`渐进式提示 (${slug ? (hintLevels[slug] || 0) : 0}/3)`}
            >
              {hintLoading ? <Loader2 className="w-3 h-3 animate-spin" /> : <Lightbulb className="w-3 h-3" />}
              提示 {slug ? (hintLevels[slug] || 0) : 0}/3
            </button>
            <button
              onClick={handleResetCode}
              className="flex items-center gap-1 px-2 py-1 text-xs text-gray-400 hover:text-gray-200 hover:bg-gray-700 rounded transition"
              title="重置为初始代码模板"
            >
              <RotateCcw className="w-3 h-3" />
              重置
            </button>
            <select
              value={language.value}
              onChange={(e) => handleLanguageChange(e.target.value)}
              className="px-3 py-1 text-xs bg-gray-700 border border-gray-600 rounded text-gray-200 focus:outline-none"
            >
              {LANGUAGES.map((l) => (
                <option key={l.value} value={l.value}>{l.label}</option>
              ))}
            </select>
          </div>

          {/* Monaco 编辑器 */}
          <div className="flex-1 min-h-0">
            <Editor
              height="100%"
              language={monacoLang}
              theme="vs-dark"
              value={code}
              onChange={handleCodeChange}
              options={{
                fontSize: 14,
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                tabSize: 4,
              }}
            />
          </div>

          {/* 提交按钮 */}
          <div className="flex justify-center py-2 bg-gray-800 border-t border-gray-700 shrink-0">
            <button
              onClick={handleSubmit}
              disabled={submitting || !code.trim()}
              className="flex items-center gap-2 px-8 py-2 bg-green-600 rounded text-sm font-medium hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
            >
              {submitting ? (
                <><Loader2 className="w-4 h-4 animate-spin" />判题中...</>
              ) : (
                <><Send className="w-4 h-4" />提交代码</>
              )}
            </button>
          </div>

          {/* 垂直拖拽手柄 + 聊天框收放按钮 */}
          <div className="flex items-center shrink-0 bg-gray-700">
            <div
              className="flex-1 h-1 cursor-row-resize hover:bg-indigo-500 transition-colors"
              onMouseDown={handleVerticalDragStart}
            />
            <button
              onClick={() => {
                if (!chatCollapsed) {
                  savedChatHeightRef.current = chatHeight
                  setChatCollapsed(true)
                } else {
                  setChatCollapsed(false)
                  setChatHeight(savedChatHeightRef.current)
                }
              }}
              className="px-2 py-0.5 text-gray-400 hover:text-gray-200 transition"
              title={chatCollapsed ? '展开聊天框' : '收起聊天框'}
            >
              {chatCollapsed
                ? <ChevronUp className="w-3.5 h-3.5" />
                : <ChevronDown className="w-3.5 h-3.5" />
              }
            </button>
            <div
              className="flex-1 h-1 cursor-row-resize hover:bg-indigo-500 transition-colors"
              onMouseDown={handleVerticalDragStart}
            />
          </div>

          {/* Dify 聊天框 */}
          <div
            className="flex flex-col shrink-0 overflow-hidden transition-[height] duration-200"
            style={{ height: chatCollapsed ? 32 : chatHeight }}
          >
            <div
              className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-gray-400 border-b border-gray-700 bg-gray-800 cursor-pointer select-none"
              onClick={() => {
                if (!chatCollapsed) {
                  savedChatHeightRef.current = chatHeight
                  setChatCollapsed(true)
                } else {
                  setChatCollapsed(false)
                  setChatHeight(savedChatHeightRef.current)
                }
              }}
            >
              <MessageSquare className="w-3 h-3" />
              AI 助手
              {chatLoading && <Loader2 className="w-3 h-3 animate-spin ml-auto" />}
            </div>

            <div className="flex-1 overflow-y-auto p-2 space-y-2">
              {chatMessages.length === 0 ? (
                <p className="text-xs text-gray-500 p-1">提交代码后会在此显示分析结果，也可以直接提问</p>
              ) : (
                chatMessages.map((msg) => (
                  <div key={msg.id} className={`flex gap-1.5 ${msg.role === 'user' ? 'justify-end' : ''}`}>
                    {msg.role !== 'user' && (
                      <span className="shrink-0 mt-0.5">
                        {msg.role === 'assistant'
                          ? <Bot className="w-3.5 h-3.5 text-indigo-400" />
                          : <span className="w-3.5 h-3.5 block" />}
                      </span>
                    )}
                    <div className={`text-xs rounded px-2 py-1.5 max-w-[85%] ${
                      msg.role === 'user'
                        ? 'bg-indigo-600 text-white whitespace-pre-wrap'
                        : msg.role === 'assistant'
                          ? 'bg-gray-700 text-gray-200 chat-markdown'
                          : 'bg-gray-800 text-gray-400 italic whitespace-pre-wrap'
                    }`}>
                      {msg.role === 'assistant'
                        ? <Markdown remarkPlugins={[remarkMath]} rehypePlugins={[rehypeKatex]}>{msg.content}</Markdown>
                        : msg.content
                      }
                    </div>
                    {msg.role === 'user' && (
                      <User className="w-3.5 h-3.5 text-indigo-400 shrink-0 mt-0.5" />
                    )}
                  </div>
                ))
              )}
              <div ref={chatEndRef} />
            </div>

            <div className="flex items-center gap-2 px-2 py-1.5 border-t border-gray-700 bg-gray-800">
              <input
                type="text"
                placeholder="输入问题..."
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && handleSendChat()}
                disabled={chatLoading || !currentSession}
                className="flex-1 px-2 py-1 text-xs bg-gray-700 border border-gray-600 rounded text-gray-200 placeholder-gray-500 focus:outline-none focus:border-indigo-500 disabled:opacity-50"
              />
              <button
                onClick={handleSendChat}
                disabled={chatLoading || !chatInput.trim() || !currentSession}
                className="p-1.5 bg-indigo-600 rounded hover:bg-indigo-700 disabled:opacity-50 transition"
              >
                <Send className="w-3 h-3" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
