import { useState, useEffect } from 'react'
import {
  Clock, X, ChevronDown, ChevronRight, Trash2,
} from 'lucide-react'
import { confirm } from '@/store/uiStore'
import type { SessionChain, SessionTrackItem } from '@/types'

/** 难度颜色 */
function diffColor(d: string) {
  if (d === 'Easy') return 'text-emerald-400'
  if (d === 'Medium') return 'text-yellow-400'
  if (d === 'Hard') return 'text-red-400'
  return 'theme-faint'
}

function diffBg(d: string) {
  if (d === 'Easy') return 'diff-easy'
  if (d === 'Medium') return 'diff-medium'
  if (d === 'Hard') return 'diff-hard'
  return 'theme-tag'
}

interface TrackSidebarProps {
  open: boolean
  onClose: () => void
  chains: SessionChain[]
  chainsLoading: boolean
  currentSessionId: number | null
  currentSlug: string | undefined
  sessionTrack: SessionTrackItem[]
  onNavigate: (slug: string, sessionId: number) => void
  onDeleteChain: (sessionId: number) => void
}

export default function TrackSidebar({
  open, onClose, chains, chainsLoading, currentSessionId,
  currentSlug, sessionTrack, onNavigate, onDeleteChain,
}: TrackSidebarProps) {
  const [expanded, setExpanded] = useState<Set<number>>(new Set())

  // 当前链默认展开
  useEffect(() => {
    if (currentSessionId) {
      setExpanded((prev) => {
        const next = new Set(prev)
        next.add(currentSessionId)
        return next
      })
    }
  }, [currentSessionId])

  const toggle = (sessionId: number) => {
    setExpanded((prev) => {
      const next = new Set(prev)
      if (next.has(sessionId)) next.delete(sessionId)
      else next.add(sessionId)
      return next
    })
  }

  const handleDelete = async (e: React.MouseEvent, sessionId: number) => {
    e.stopPropagation()
    if (await confirm('确定删除这条练习轨迹链吗？相关的 AI 对话记录也会清除。', { type: 'danger', confirmText: '删除' })) {
      onDeleteChain(sessionId)
    }
  }

  return (
    <aside
      className={`shrink-0 theme-sidebar overflow-hidden transition-[width] duration-300 ease-in-out ${
        open ? 'w-64' : 'w-0 !border-r-0'
      }`}
    >
      <div className="w-64 h-full flex flex-col">
        {/* 标题栏 */}
        <div className="flex items-center justify-between px-3 py-2.5 border-b theme-border">
          <div className="flex items-center gap-1.5">
            <Clock className="w-3.5 h-3.5 theme-accent-text" />
            <span className="text-xs font-semibold theme-muted">练习轨迹</span>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded theme-hover transition theme-hint"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* 链列表 */}
        <div className="flex-1 overflow-y-auto">
          {chainsLoading ? (
            <p className="text-xs theme-faint text-center py-6">加载中...</p>
          ) : chains.length === 0 ? (
            <p className="text-xs theme-faint text-center py-6">暂无轨迹记录</p>
          ) : (
            chains.map((chain) => {
              const isCurrent = chain.sessionId === currentSessionId
              const isExpanded = expanded.has(chain.sessionId)
              // 如果是当前链，用 sessionTrack（实时更新）；否则只显示概要
              const trackItems = isCurrent ? sessionTrack : null

              return (
                <div key={chain.sessionId} className={`border-b theme-border ${isCurrent ? 'bg-[var(--accent-soft)]' : ''}`}>
                  {/* 链标题行 */}
                  <div
                    className="flex items-center gap-1 px-3 py-2 cursor-pointer theme-hover transition group"
                    onClick={() => toggle(chain.sessionId)}
                  >
                    {isExpanded
                      ? <ChevronDown className="w-3 h-3 theme-hint shrink-0" />
                      : <ChevronRight className="w-3 h-3 theme-hint shrink-0" />}

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-1.5">
                        <span className={`text-[11px] font-mono ${isCurrent ? 'theme-accent-text' : 'theme-faint'}`}>
                          #{chain.headFrontendId}
                        </span>
                        <span className={`text-xs truncate ${isCurrent ? 'theme-text font-medium' : 'theme-muted'}`}>
                          {chain.headTitle}
                        </span>
                      </div>
                      <div className="flex items-center gap-1.5 mt-0.5">
                        <span className={`text-[10px] ${diffColor(chain.headDifficulty)}`}>
                          {chain.headDifficulty}
                        </span>
                        {!isExpanded && chain.problemCount > 1 && (
                          <span className="text-[10px] theme-hint">{chain.problemCount}题</span>
                        )}
                      </div>
                    </div>

                    <button
                      onClick={(e) => handleDelete(e, chain.sessionId)}
                      className="p-1 rounded opacity-0 group-hover:opacity-100 theme-hover transition theme-hint hover:text-[var(--danger)]"
                      title="删除轨迹链"
                    >
                      <Trash2 className="w-3 h-3" />
                    </button>
                  </div>

                  {/* 展开后的题目列表 */}
                  {isExpanded && (
                    <div className="pb-2">
                      {trackItems ? (
                        /* 当前链：用实时 sessionTrack 数据 */
                        <TrackTimeline
                          items={trackItems}
                          currentSlug={currentSlug}
                          onNavigate={(slug) => onNavigate(slug, chain.sessionId)}
                        />
                      ) : (
                        /* 其他链：显示头题 + 数量提示，点击加载 */
                        <div className="px-3 pl-7">
                          <button
                            className="text-[11px] theme-faint hover:text-[var(--accent)] transition py-1"
                            onClick={() => onNavigate(chain.headSlug, chain.sessionId)}
                          >
                            #{chain.headFrontendId} {chain.headTitle}
                          </button>
                          {chain.problemCount > 1 && (
                            <p className="text-[10px] theme-hint mt-0.5">
                              共 {chain.problemCount} 道题 · 点击跳转并加载详情
                            </p>
                          )}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )
            })
          )}
        </div>
      </div>
    </aside>
  )
}

/** 轨迹时间线（当前链展开后的题目列表） */
function TrackTimeline({
  items, currentSlug, onNavigate,
}: {
  items: SessionTrackItem[]
  currentSlug: string | undefined
  onNavigate: (slug: string) => void
}) {
  if (items.length === 0) {
    return <p className="text-[10px] theme-hint text-center py-2">暂无题目</p>
  }

  return (
    <div className="relative ml-5 pl-4" style={{ borderLeft: '1px solid var(--border-color)' }}>
      {items.map((item, idx) => {
        const isActive = item.slug === currentSlug
        const isInitial = item.jumpType === 'initial'

        return (
          <div key={item.id} className={`relative ${idx > 0 ? 'mt-2' : ''}`}>
            {/* 时间线圆点 */}
            <span
              className={`absolute -left-[18px] top-1 w-2 h-2 rounded-full ${
                isActive
                  ? 'bg-[var(--accent)]'
                  : isInitial
                    ? 'bg-[var(--text-muted)]'
                    : 'bg-emerald-500'
              }`}
              style={{ boxShadow: `0 0 0 2px var(--sidebar-bg)` }}
            />

            <button
              onClick={() => onNavigate(item.slug)}
              className={`w-full text-left rounded px-1.5 py-1 theme-hover transition ${
                isActive ? 'bg-[var(--hover-bg)]' : ''
              }`}
            >
              <div className="flex items-center gap-1">
                <span
                  className={`text-[9px] leading-none px-1 py-0.5 rounded ${
                    isInitial
                      ? 'theme-tag'
                      : 'theme-status-accepted'
                  }`}
                >
                  {isInitial ? '首次' : '推荐'}
                </span>
                <span className={`text-[10px] px-1 py-0.5 rounded ${diffBg(item.difficulty)}`}>
                  {item.difficulty}
                </span>
                {item.accepted ? (
                  <span className="text-[9px] leading-none px-1 py-0.5 rounded theme-status-accepted font-medium">AC</span>
                ) : item.attemptCount > 0 ? (
                  <span className="text-[9px] leading-none px-1 py-0.5 rounded theme-status-error font-medium">WA</span>
                ) : null}
              </div>
              <p className={`text-[11px] mt-0.5 truncate ${
                isActive ? 'theme-text font-medium' : 'theme-faint'
              }`}>
                #{item.frontendId} {item.title}
              </p>
            </button>
          </div>
        )
      })}
    </div>
  )
}
