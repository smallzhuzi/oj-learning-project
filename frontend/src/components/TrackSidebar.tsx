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
  return 'text-gray-400'
}

function diffBg(d: string) {
  if (d === 'Easy') return 'bg-emerald-900/40 text-emerald-400'
  if (d === 'Medium') return 'bg-yellow-900/40 text-yellow-400'
  if (d === 'Hard') return 'bg-red-900/40 text-red-400'
  return 'bg-gray-700 text-gray-400'
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
      className={`shrink-0 bg-gray-800 border-r border-gray-700 overflow-hidden transition-[width] duration-300 ease-in-out ${
        open ? 'w-64' : 'w-0 border-r-0'
      }`}
    >
      <div className="w-64 h-full flex flex-col">
        {/* 标题栏 */}
        <div className="flex items-center justify-between px-3 py-2.5 border-b border-gray-700">
          <div className="flex items-center gap-1.5">
            <Clock className="w-3.5 h-3.5 text-indigo-400" />
            <span className="text-xs font-semibold text-gray-300">练习轨迹</span>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded hover:bg-gray-700 transition text-gray-500 hover:text-gray-300"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* 链列表 */}
        <div className="flex-1 overflow-y-auto">
          {chainsLoading ? (
            <p className="text-xs text-gray-500 text-center py-6">加载中...</p>
          ) : chains.length === 0 ? (
            <p className="text-xs text-gray-500 text-center py-6">暂无轨迹记录</p>
          ) : (
            chains.map((chain) => {
              const isCurrent = chain.sessionId === currentSessionId
              const isExpanded = expanded.has(chain.sessionId)
              // 如果是当前链，用 sessionTrack（实时更新）；否则只显示概要
              const trackItems = isCurrent ? sessionTrack : null

              return (
                <div key={chain.sessionId} className={`border-b border-gray-700/50 ${isCurrent ? 'bg-indigo-900/20' : ''}`}>
                  {/* 链标题行 */}
                  <div
                    className="flex items-center gap-1 px-3 py-2 cursor-pointer hover:bg-gray-700/50 transition group"
                    onClick={() => toggle(chain.sessionId)}
                  >
                    {isExpanded
                      ? <ChevronDown className="w-3 h-3 text-gray-500 shrink-0" />
                      : <ChevronRight className="w-3 h-3 text-gray-500 shrink-0" />}

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-1.5">
                        <span className={`text-[11px] font-mono ${isCurrent ? 'text-indigo-300' : 'text-gray-400'}`}>
                          #{chain.headFrontendId}
                        </span>
                        <span className={`text-xs truncate ${isCurrent ? 'text-gray-100 font-medium' : 'text-gray-300'}`}>
                          {chain.headTitle}
                        </span>
                      </div>
                      <div className="flex items-center gap-1.5 mt-0.5">
                        <span className={`text-[10px] ${diffColor(chain.headDifficulty)}`}>
                          {chain.headDifficulty}
                        </span>
                        {!isExpanded && chain.problemCount > 1 && (
                          <span className="text-[10px] text-gray-500">{chain.problemCount}题</span>
                        )}
                      </div>
                    </div>

                    <button
                      onClick={(e) => handleDelete(e, chain.sessionId)}
                      className="p-1 rounded opacity-0 group-hover:opacity-100 hover:bg-gray-600 transition text-gray-500 hover:text-red-400"
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
                            className="text-[11px] text-gray-400 hover:text-indigo-300 transition py-1"
                            onClick={() => onNavigate(chain.headSlug, chain.sessionId)}
                          >
                            #{chain.headFrontendId} {chain.headTitle}
                          </button>
                          {chain.problemCount > 1 && (
                            <p className="text-[10px] text-gray-500 mt-0.5">
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
    return <p className="text-[10px] text-gray-500 text-center py-2">暂无题目</p>
  }

  return (
    <div className="relative ml-5 pl-4 border-l border-gray-600">
      {items.map((item, idx) => {
        const isActive = item.slug === currentSlug
        const isInitial = item.jumpType === 'initial'

        return (
          <div key={item.id} className={`relative ${idx > 0 ? 'mt-2' : ''}`}>
            {/* 时间线圆点 */}
            <span
              className={`absolute -left-[18px] top-1 w-2 h-2 rounded-full ring-2 ring-gray-800 ${
                isActive
                  ? 'bg-indigo-500'
                  : isInitial
                    ? 'bg-gray-500'
                    : 'bg-emerald-500'
              }`}
            />

            <button
              onClick={() => onNavigate(item.slug)}
              className={`w-full text-left rounded px-1.5 py-1 hover:bg-gray-700/60 transition ${
                isActive ? 'bg-gray-700/40' : ''
              }`}
            >
              <div className="flex items-center gap-1">
                <span
                  className={`text-[9px] leading-none px-1 py-0.5 rounded ${
                    isInitial
                      ? 'bg-gray-700 text-gray-400'
                      : 'bg-emerald-900/40 text-emerald-400'
                  }`}
                >
                  {isInitial ? '首次' : '推荐'}
                </span>
                <span className={`text-[10px] px-1 py-0.5 rounded ${diffBg(item.difficulty)}`}>
                  {item.difficulty}
                </span>
                {item.accepted ? (
                  <span className="text-[9px] leading-none px-1 py-0.5 rounded bg-emerald-900/40 text-emerald-400 font-medium">AC</span>
                ) : item.attemptCount > 0 ? (
                  <span className="text-[9px] leading-none px-1 py-0.5 rounded bg-red-900/40 text-red-400 font-medium">WA</span>
                ) : null}
              </div>
              <p className={`text-[11px] mt-0.5 truncate ${
                isActive ? 'text-gray-100 font-medium' : 'text-gray-400'
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
