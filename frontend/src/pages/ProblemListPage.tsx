import { useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Search,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  CheckCircle2,
  MinusCircle,
  History,
  X,
  Loader2,
  Zap,
  Trophy,
  RotateCcw,
  Check,
} from 'lucide-react'
import { useProblemStore } from '@/store/problemStore'
import { useUserStore } from '@/store/userStore'
import { getUserStatusMap } from '@/api/submission'
import { getUserChains } from '@/api/session'
import { searchProblemTags } from '@/api/problem'
import UserMenu from '@/components/UserMenu'
import ThemeToggle from '@/components/ThemeToggle'
import type { ProblemTagOption, SessionChain } from '@/types'

const difficultyColor: Record<string, string> = {
  Easy: 'text-green-300 bg-green-500/12',
  Medium: 'text-amber-300 bg-amber-500/12',
  Hard: 'text-rose-300 bg-rose-500/12',
  入门: 'text-rose-300 bg-rose-500/12',
  '普及-': 'text-orange-300 bg-orange-500/12',
  '普及/提高-': 'text-amber-300 bg-amber-500/12',
  '普及+/提高': 'text-emerald-300 bg-emerald-500/12',
  '提高+/省选-': 'text-sky-300 bg-sky-500/12',
  '省选/NOI-': 'text-violet-300 bg-violet-500/12',
  'NOI/NOI+/CTSC': 'text-slate-100 bg-slate-500/18',
  暂无评定: 'text-slate-300 bg-slate-500/12',
}

const tagTypeLabel: Record<string, string> = {
  knowledge: '知识点',
  source: '来源',
  region: '区域',
  year: '时间',
  special: '特殊题目',
  category: '其他',
}

const TAG_MODAL_PAGE_SIZE = 20

export default function ProblemListPage() {
  const navigate = useNavigate()
  const {
    problems,
    total,
    currentPage,
    loading,
    ojPlatform,
    fetchProblems,
    setOjPlatform,
    setCurrentPage,
  } = useProblemStore()
  const { user } = useUserStore()

  const [keyword, setKeyword] = useState('')
  const [selectedTags, setSelectedTags] = useState<ProblemTagOption[]>([])
  const [difficulty, setDifficulty] = useState('')
  const [pageSize, setPageSize] = useState(20)
  const [statusMap, setStatusMap] = useState<Record<string, string>>({})
  const [chainsOpen, setChainsOpen] = useState(false)
  const [chains, setChains] = useState<SessionChain[]>([])
  const [chainsLoading, setChainsLoading] = useState(false)

  const [tagModalOpen, setTagModalOpen] = useState(false)
  const [tagKeyword, setTagKeyword] = useState('')
  const [tagOptions, setTagOptions] = useState<ProblemTagOption[]>([])
  const [tagLoading, setTagLoading] = useState(false)
  const [tagModalPage, setTagModalPage] = useState(1)
  const [tagModalTotalPages, setTagModalTotalPages] = useState(1)
  const [draftTags, setDraftTags] = useState<ProblemTagOption[]>([])
  const firstOpenLoadedRef = useRef(false)
  const tagRequestIdRef = useRef(0)

  const loadProblems = (page = 1, size = pageSize) => {
    setCurrentPage(page)
    fetchProblems({
      keyword: keyword || undefined,
      difficulty: difficulty || undefined,
      tags: selectedTags.length > 0 ? selectedTags.map((tag) => tag.key) : undefined,
      ojPlatform,
      pageNum: page,
      pageSize: size,
    })
  }

  const loadTagOptions = async (search = '', page = 1) => {
    const requestId = ++tagRequestIdRef.current
    setTagLoading(true)
    try {
      const res = await searchProblemTags(ojPlatform, search || undefined, page, TAG_MODAL_PAGE_SIZE)
      if (requestId !== tagRequestIdRef.current) {
        return
      }
      if (res.code === 200) {
        setTagOptions(res.data.records || [])
        setTagModalPage(res.data.current || page)
        setTagModalTotalPages(Math.max(1, res.data.pages || 1))
      }
    } finally {
      if (requestId === tagRequestIdRef.current) {
        setTagLoading(false)
      }
    }
  }

  const openTagModal = () => {
    setDraftTags(selectedTags)
    setTagKeyword('')
    setTagModalPage(1)
    setTagModalTotalPages(1)
    setTagModalOpen(true)
    if (!firstOpenLoadedRef.current) {
      firstOpenLoadedRef.current = true
      void loadTagOptions('', 1)
    }
  }

  const closeTagModal = () => {
    setTagModalOpen(false)
    setDraftTags([])
    setTagKeyword('')
    setTagModalPage(1)
    setTagModalTotalPages(1)
    tagRequestIdRef.current += 1
    setTagLoading(false)
  }

  const resetTagModal = () => {
    setDraftTags([])
    setTagKeyword('')
    void loadTagOptions('', 1)
  }

  const confirmTagModal = () => {
    setSelectedTags(draftTags)
    setTagModalOpen(false)
    setTagKeyword('')
    setTagModalPage(1)
  }

  const toggleDraftTag = (option: ProblemTagOption) => {
    setDraftTags((prev) => {
      const exists = prev.some((item) => item.key === option.key)
      return exists ? prev.filter((item) => item.key !== option.key) : [...prev, option]
    })
  }

  const summarizedTags = useMemo(() => {
    if (selectedTags.length === 0) return '按标签筛选'
    if (selectedTags.length === 1) return selectedTags[0].label
    if (selectedTags.length === 2) return `${selectedTags[0].label}、${selectedTags[1].label}`
    return `${selectedTags[0].label}、${selectedTags[1].label}...`
  }, [selectedTags])

  const tagSummaryTitle = useMemo(
    () => selectedTags.map((tag) => tag.label).join('、'),
    [selectedTags],
  )

  const draftTagPreview = useMemo(() => {
    if (draftTags.length === 0) return []
    return draftTags.slice(0, 3)
  }, [draftTags])

  useEffect(() => {
    setDifficulty('')
    setSelectedTags([])
    setTagModalOpen(false)
    setTagKeyword('')
    setTagOptions([])
    setDraftTags([])
    setTagModalPage(1)
    setTagModalTotalPages(1)
    firstOpenLoadedRef.current = false
  }, [ojPlatform])

  useEffect(() => {
    loadProblems(1)
  }, [ojPlatform, difficulty])

  useEffect(() => {
    getUserStatusMap().then((res) => {
      if (res.code === 200) setStatusMap(res.data || {})
    }).catch(() => {})
  }, [])

  const totalPages = Math.ceil(total / pageSize)

  const handleClickProblem = (slug: string) => {
    navigate(`/problem/${slug}`)
  }

  const loadChains = () => {
    setChainsLoading(true)
    getUserChains().then((res) => {
      if (res.code === 200) setChains(res.data || [])
    }).catch(() => {}).finally(() => setChainsLoading(false))
  }

  return (
    <div className="theme-page theme-dot-grid">
      <div className="max-w-6xl mx-auto px-6 py-8 relative">
        <div className="mb-8 relative z-40 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <select
              value={ojPlatform}
              onChange={(e) => setOjPlatform(e.target.value)}
              className="theme-input px-4 py-2 rounded-xl text-sm"
            >
              <option value="leetcode">LeetCode</option>
              <option value="luogu">洛谷</option>
              <option value="codeforces" disabled>Codeforces（即将支持）</option>
            </select>

          </div>

          {user && (
            <div className="flex items-center gap-2">
              <button
                onClick={() => { setChainsOpen(true); loadChains() }}
                className="theme-button-secondary flex items-center gap-1.5 rounded-xl px-3 py-2 text-sm"
              >
                <History className="w-3.5 h-3.5" />
                练习轨迹
              </button>
              <button
                onClick={() => navigate('/problem-sets')}
                className="theme-button-secondary flex items-center gap-1.5 rounded-xl px-3 py-2 text-sm"
              >
                <Zap className="w-3.5 h-3.5" />
                智能组题
              </button>
              <button
                onClick={() => navigate('/contests')}
                className="theme-button-secondary flex items-center gap-1.5 rounded-xl px-3 py-2 text-sm"
              >
                <Trophy className="w-3.5 h-3.5" />
                比赛
              </button>
              <ThemeToggle />
              <UserMenu />
            </div>
          )}
        </div>

        <div className="theme-panel rounded-[28px] p-5 mb-6 relative z-10">
          <div className="mb-5">
            <div>
              <div className="text-xs uppercase tracking-[0.22em] theme-faint mb-2">Problem Atlas</div>
              <h1 className="text-2xl font-semibold">题库筛选</h1>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-[minmax(0,1.3fr)_minmax(280px,0.9fr)_180px_120px] gap-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 theme-faint" />
              <input
                type="text"
                placeholder="按题号或标题搜索"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && loadProblems(1)}
                className="theme-input w-full rounded-xl pl-10 pr-4 py-3 text-sm"
              />
            </div>

            <button
              type="button"
              onClick={openTagModal}
              title={tagSummaryTitle}
              className="theme-input w-full rounded-xl px-4 py-3 text-left text-sm transition hover:brightness-110 overflow-hidden"
            >
              <div className="flex items-center justify-between gap-3 overflow-hidden">
                <span className="truncate whitespace-nowrap">{summarizedTags}</span>
                {selectedTags.length > 0 ? (
                  <span className="theme-chip rounded-full px-2 py-0.5 text-xs">{selectedTags.length}</span>
                ) : null}
              </div>
            </button>

            <select
              value={difficulty}
              onChange={(e) => setDifficulty(e.target.value)}
              className="theme-input rounded-xl px-4 py-3 text-sm"
            >
              <option value="">全部难度</option>
              {ojPlatform === 'luogu' ? (
                <>
                  <option value="入门">入门</option>
                  <option value="普及-">普及-</option>
                  <option value="普及/提高-">普及/提高-</option>
                  <option value="普及+/提高">普及+/提高</option>
                  <option value="提高+/省选-">提高+/省选-</option>
                  <option value="省选/NOI-">省选/NOI-</option>
                  <option value="NOI/NOI+/CTSC">NOI/NOI+/CTSC</option>
                </>
              ) : (
                <>
                  <option value="Easy">简单</option>
                  <option value="Medium">中等</option>
                  <option value="Hard">困难</option>
                </>
              )}
            </select>

            <button
              onClick={() => loadProblems(1)}
              className="theme-button-primary rounded-xl px-5 py-3 text-sm font-medium"
            >
              搜索
            </button>
          </div>
        </div>

        <div className="theme-panel rounded-[28px] overflow-hidden">
          <table className="w-full">
            <thead className="theme-table-head theme-divider border-b">
              <tr>
                <th className="px-4 py-3 text-center text-xs font-medium uppercase w-12 theme-faint">状态</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase theme-faint">题号</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase theme-faint">标题</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase theme-faint">难度</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase theme-faint">通过率</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase theme-faint">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y theme-divider">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center theme-faint">加载中...</td>
                </tr>
              ) : problems.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center theme-faint">暂无题目，请先同步题库</td>
                </tr>
              ) : (
                problems.map((p) => {
                  const st = statusMap[p.slug]
                  return (
                    <tr
                      key={p.id}
                      className="theme-row cursor-pointer transition"
                      onClick={() => handleClickProblem(p.slug)}
                    >
                      <td className="px-4 py-4 text-center">
                        {st === 'accepted' ? (
                          <CheckCircle2 className="w-4 h-4 text-emerald-400 mx-auto" />
                        ) : st === 'attempted' ? (
                          <MinusCircle className="w-4 h-4 text-amber-400 mx-auto" />
                        ) : null}
                      </td>
                      <td className="px-6 py-4 text-sm theme-muted">{p.frontendId}</td>
                      <td className="px-6 py-4 text-sm font-medium">{p.title}</td>
                      <td className="px-6 py-4">
                        <span className={`px-2.5 py-1 rounded-full text-xs font-medium ${difficultyColor[p.difficulty] || 'text-slate-300 bg-slate-500/12'}`}>
                          {p.difficulty}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm theme-muted">
                        {p.acceptanceRate != null ? `${p.acceptanceRate}%` : '-'}
                      </td>
                      <td className="px-6 py-4">
                        <button
                          onClick={(e) => {
                            e.stopPropagation()
                            handleClickProblem(p.slug)
                          }}
                          className="text-sm theme-accent-text hover:brightness-110"
                        >
                          做题
                        </button>
                      </td>
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>
        </div>

        {totalPages >= 1 && (
          <div className="mt-5 flex items-center justify-between">
            <span className="text-sm theme-faint">共 {total} 题</span>

            <div className="flex items-center gap-1">
              <button
                disabled={currentPage <= 1}
                onClick={() => loadProblems(1)}
                className="theme-button-secondary p-1.5 rounded-lg disabled:opacity-30"
                title="首页"
              >
                <ChevronsLeft className="w-4 h-4" />
              </button>
              <button
                disabled={currentPage <= 1}
                onClick={() => loadProblems(currentPage - 1)}
                className="theme-button-secondary p-1.5 rounded-lg disabled:opacity-30"
                title="上一页"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>

              {(() => {
                const pages: (number | '...')[] = []
                const range = 2
                const left = Math.max(1, currentPage - range)
                const right = Math.min(totalPages, currentPage + range)

                if (left > 1) {
                  pages.push(1)
                  if (left > 2) pages.push('...')
                }
                for (let i = left; i <= right; i++) pages.push(i)
                if (right < totalPages) {
                  if (right < totalPages - 1) pages.push('...')
                  pages.push(totalPages)
                }

                return pages.map((p, idx) =>
                  p === '...' ? (
                    <span key={`dot-${idx}`} className="w-8 text-center theme-faint text-sm select-none">...</span>
                  ) : (
                    <button
                      key={p}
                      onClick={() => loadProblems(p)}
                      className={`w-8 h-8 text-sm rounded-lg transition ${
                        p === currentPage ? 'theme-button-primary' : 'theme-button-secondary'
                      }`}
                    >
                      {p}
                    </button>
                  ),
                )
              })()}

              <button
                disabled={currentPage >= totalPages}
                onClick={() => loadProblems(currentPage + 1)}
                className="theme-button-secondary p-1.5 rounded-lg disabled:opacity-30"
                title="下一页"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
              <button
                disabled={currentPage >= totalPages}
                onClick={() => loadProblems(totalPages)}
                className="theme-button-secondary p-1.5 rounded-lg disabled:opacity-30"
                title="末页"
              >
                <ChevronsRight className="w-4 h-4" />
              </button>
            </div>

            <div className="flex items-center gap-2 text-sm theme-faint">
              <span>每页</span>
              <select
                value={pageSize}
                onChange={(e) => {
                  const size = Number(e.target.value)
                  setPageSize(size)
                  loadProblems(1, size)
                }}
                className="theme-input rounded-lg px-2 py-1 text-sm"
              >
                {[10, 20, 50, 100].map((n) => (
                  <option key={n} value={n}>{n}</option>
                ))}
              </select>
              <span>题</span>
            </div>
          </div>
        )}
      </div>

      {tagModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center theme-overlay animate-scale-in" onClick={closeTagModal}>
          <div
            className="theme-panel-strong w-[760px] max-h-[84vh] rounded-[30px] overflow-hidden flex flex-col"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="px-6 py-5 border-b theme-divider flex items-start justify-between">
              <div>
                <div className="text-xs uppercase tracking-[0.22em] theme-faint mb-2">Tag Selector</div>
                <h3 className="text-xl font-semibold">选择题库标签</h3>
                <p className="mt-1 text-sm theme-muted">支持搜索、多选和分页浏览。确认后才会应用到题库页。</p>
              </div>
              <button onClick={closeTagModal} className="theme-button-secondary rounded-xl p-2">
                <X size={16} />
              </button>
            </div>

            <div className="px-6 py-4 border-b theme-divider">
              <div className="grid grid-cols-[minmax(0,1fr)_120px_120px] gap-3">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 theme-faint" />
                  <input
                    value={tagKeyword}
                    onChange={(e) => setTagKeyword(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        void loadTagOptions(tagKeyword, 1)
                      }
                    }}
                    className="theme-input w-full rounded-xl pl-10 pr-4 py-3 text-sm"
                    placeholder="搜索标签名称、别名或来源名"
                  />
                </div>
                <button
                  type="button"
                  onClick={() => void loadTagOptions(tagKeyword, 1)}
                  className="theme-button-primary rounded-xl px-4 py-3 text-sm"
                >
                  搜索
                </button>
                <button
                  type="button"
                  onClick={resetTagModal}
                  className="theme-button-secondary rounded-xl px-4 py-3 text-sm inline-flex items-center justify-center gap-2"
                >
                  <RotateCcw className="w-4 h-4" />
                  重置
                </button>
              </div>

              <div className="mt-4 h-8 overflow-hidden">
                {draftTags.length > 0 ? (
                  <div className="flex items-center gap-2 overflow-hidden">
                    {draftTagPreview.map((tag) => (
                      <span key={tag.key} className="theme-chip inline-flex max-w-[180px] items-center gap-1 rounded-full px-2.5 py-1 text-xs">
                        <span className="truncate">{tag.label}</span>
                        <button type="button" onClick={() => toggleDraftTag(tag)}>
                          <X className="w-3 h-3" />
                        </button>
                      </span>
                    ))}
                    {draftTags.length > draftTagPreview.length ? (
                      <span className="theme-faint text-xs whitespace-nowrap">+{draftTags.length - draftTagPreview.length} 个已选</span>
                    ) : null}
                  </div>
                ) : (
                  <span className="theme-faint text-xs">未选择任何标签</span>
                )}
              </div>
            </div>

            <div className="h-[420px] overflow-y-auto px-4 py-4">
              {tagLoading && (
                <div className="mb-3 flex items-center justify-end text-xs theme-faint">
                  <Loader2 className="animate-spin mr-2 h-3.5 w-3.5" />
                  正在刷新标签列表
                </div>
              )}

              {tagOptions.length === 0 ? (
                <div className="flex h-full items-center justify-center text-sm theme-faint">没有匹配标签</div>
              ) : (
                <div className="grid grid-cols-2 gap-3">
                  {tagOptions.map((option) => {
                    const selected = draftTags.some((tag) => tag.key === option.key)
                    return (
                      <button
                        key={option.key}
                        type="button"
                        onClick={() => toggleDraftTag(option)}
                        className={`rounded-2xl border px-4 py-4 text-left transition ${
                          selected ? 'theme-chip' : 'theme-surface hover:brightness-110'
                        }`}
                      >
                        <div className="flex items-start justify-between gap-4">
                          <div>
                            <div className="font-medium">{option.label}</div>
                            <div className="mt-1 text-xs theme-faint">
                              {tagTypeLabel[option.type || ''] || option.type || '未分类'}
                            </div>
                          </div>
                          <span className={`mt-0.5 flex h-5 w-5 items-center justify-center rounded-full border ${
                            selected ? 'border-current' : 'border-[var(--border-color)]'
                          }`}>
                            {selected ? <Check className="w-3.5 h-3.5" /> : null}
                          </span>
                        </div>
                      </button>
                    )
                  })}
                </div>
              )}
            </div>

            <div className="px-6 py-4 border-t theme-divider flex items-center justify-between">
              <div className="flex items-center gap-2 text-sm theme-faint">
                <button
                  onClick={() => void loadTagOptions(tagKeyword, 1)}
                  disabled={tagModalPage <= 1}
                  className="theme-button-secondary rounded-lg px-2.5 py-1.5 text-xs disabled:opacity-30"
                >
                  首页
                </button>
                <button
                  onClick={() => void loadTagOptions(tagKeyword, Math.max(1, tagModalPage - 1))}
                  disabled={tagModalPage <= 1}
                  className="theme-button-secondary rounded-lg px-2.5 py-1.5 text-xs disabled:opacity-30"
                >
                  上一页
                </button>
                <span>{tagModalPage} / {tagModalTotalPages}</span>
                <button
                  onClick={() => void loadTagOptions(tagKeyword, Math.min(tagModalTotalPages, tagModalPage + 1))}
                  disabled={tagModalPage >= tagModalTotalPages}
                  className="theme-button-secondary rounded-lg px-2.5 py-1.5 text-xs disabled:opacity-30"
                >
                  下一页
                </button>
                <button
                  onClick={() => void loadTagOptions(tagKeyword, tagModalTotalPages)}
                  disabled={tagModalPage >= tagModalTotalPages}
                  className="theme-button-secondary rounded-lg px-2.5 py-1.5 text-xs disabled:opacity-30"
                >
                  末页
                </button>
              </div>

              <div className="flex items-center gap-2">
                <button onClick={closeTagModal} className="theme-button-secondary rounded-xl px-4 py-2 text-sm">
                  取消
                </button>
                <button
                  onClick={confirmTagModal}
                  className="theme-button-primary rounded-xl px-4 py-2 text-sm inline-flex items-center gap-2"
                >
                  <Check className="w-4 h-4" />
                  确认
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {chainsOpen && (
        <div className="fixed inset-0 z-50 flex justify-end">
          <div className="absolute inset-0 theme-overlay" onClick={() => setChainsOpen(false)} />
          <div className="theme-panel-strong relative w-80 h-full border-l flex flex-col animate-slide-in-right">
            <div className="flex items-center justify-between px-4 py-3 border-b theme-divider">
              <div className="flex items-center gap-2">
                <History className="w-4 h-4 theme-accent-text" />
                <span className="text-sm font-semibold">历史练习轨迹</span>
              </div>
              <button
                onClick={() => setChainsOpen(false)}
                className="theme-button-secondary p-1.5 rounded-lg"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto">
              {chainsLoading ? (
                <div className="flex items-center justify-center py-12 theme-faint">
                  <Loader2 className="w-5 h-5 animate-spin mr-2" />
                  加载中...
                </div>
              ) : chains.length === 0 ? (
                <div className="text-center py-12 theme-faint text-sm">
                  暂无练习记录，去题库开始做题吧
                </div>
              ) : (
                <div className="py-2">
                  {chains.map((chain) => (
                    <button
                      key={chain.sessionId}
                      onClick={() => {
                        setChainsOpen(false)
                        navigate(`/problem/${chain.headSlug}`)
                      }}
                      className="w-full text-left px-4 py-3 theme-row transition border-b theme-divider"
                    >
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-mono theme-accent-text">#{chain.headFrontendId}</span>
                        <span className="text-sm truncate flex-1">{chain.headTitle}</span>
                        <span className={`text-[10px] px-1.5 py-0.5 rounded ${difficultyColor[chain.headDifficulty] || 'text-slate-300 bg-slate-500/12'}`}>
                          {chain.headDifficulty}
                        </span>
                      </div>
                      <div className="flex items-center gap-2 mt-1">
                        <span className="text-[11px] theme-faint">{chain.problemCount} 道题</span>
                        <span className="text-[11px] theme-faint">
                          {new Date(chain.startedAt).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })}
                        </span>
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
