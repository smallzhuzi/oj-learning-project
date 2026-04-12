import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Search, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight, CheckCircle2, MinusCircle, History, X, Loader2, Zap, Trophy } from 'lucide-react'
import { useProblemStore } from '@/store/problemStore'
import { useUserStore } from '@/store/userStore'
import { getUserStatusMap } from '@/api/submission'
import { getUserChains } from '@/api/session'
import UserMenu from '@/components/UserMenu'
import type { SessionChain } from '@/types'

/** 难度标签颜色映射（深色主题，兼容 LeetCode 和洛谷难度） */
const difficultyColor: Record<string, string> = {
  // LeetCode
  Easy: 'text-green-400 bg-green-900/30',
  Medium: 'text-yellow-400 bg-yellow-900/30',
  Hard: 'text-red-400 bg-red-900/30',
  // 洛谷
  '入门': 'text-red-300 bg-red-900/30',
  '普及-': 'text-orange-300 bg-orange-900/30',
  '普及/提高-': 'text-yellow-300 bg-yellow-900/30',
  '普及+/提高': 'text-green-300 bg-green-900/30',
  '提高+/省选-': 'text-blue-300 bg-blue-900/30',
  '省选/NOI-': 'text-purple-300 bg-purple-900/30',
  'NOI/NOI+/CTSC': 'text-gray-100 bg-gray-600/50',
  '暂无评定': 'text-gray-400 bg-gray-700',
}

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
  const [difficulty, setDifficulty] = useState('')
  const [pageSize, setPageSize] = useState(20)
  const [statusMap, setStatusMap] = useState<Record<string, string>>({})
  const [chainsOpen, setChainsOpen] = useState(false)
  const [chains, setChains] = useState<SessionChain[]>([])
  const [chainsLoading, setChainsLoading] = useState(false)
  /** 加载题目列表 */
  const loadProblems = (page = 1, size = pageSize) => {
    setCurrentPage(page)
    fetchProblems({
      keyword: keyword || undefined,
      difficulty: difficulty || undefined,
      ojPlatform,
      pageNum: page,
      pageSize: size,
    })
  }

  // 切换平台时重置难度筛选
  useEffect(() => {
    setDifficulty('')
  }, [ojPlatform])

  useEffect(() => {
    loadProblems(1)
  }, [ojPlatform, difficulty])

  /** 加载用户提交状态映射 */
  useEffect(() => {
    getUserStatusMap().then((res) => {
      if (res.code === 200) setStatusMap(res.data || {})
    }).catch(() => {})
  }, [])

  const totalPages = Math.ceil(total / pageSize)

  /** 点击题目跳转到做题页面 */
  const handleClickProblem = (slug: string) => {
    navigate(`/problem/${slug}`)
  }

  /** 加载历史练习轨迹链 */
  const loadChains = () => {
    setChainsLoading(true)
    getUserChains().then((res) => {
      if (res.code === 200) setChains(res.data || [])
    }).catch(() => {}).finally(() => setChainsLoading(false))
  }

  return (
    <div className="min-h-screen bg-gray-900 text-gray-100">
      <div className="max-w-6xl mx-auto px-6 py-8">

        {/* 顶部：OJ 选择器 + 用户信息 */}
        <div className="mb-8 flex items-center justify-between">
          <select
            value={ojPlatform}
            onChange={(e) => setOjPlatform(e.target.value)}
            className="px-4 py-2 border border-gray-600 rounded-lg bg-gray-800 text-sm text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="leetcode">LeetCode</option>
            <option value="luogu">洛谷</option>
            <option value="codeforces" disabled>Codeforces（即将支持）</option>
          </select>

          {user && (
            <div className="flex items-center gap-2">
              <button
                onClick={() => { setChainsOpen(true); loadChains() }}
                className="flex items-center gap-1.5 px-3 py-1.5 text-sm text-gray-400 hover:text-gray-200 border border-gray-600 rounded-lg hover:bg-gray-700 transition"
              >
                <History className="w-3.5 h-3.5" />
                练习轨迹
              </button>
              <button
                onClick={() => navigate('/problem-sets')}
                className="flex items-center gap-1.5 px-3 py-1.5 text-sm text-gray-400 hover:text-gray-200 border border-gray-600 rounded-lg hover:bg-gray-700 transition"
              >
                <Zap className="w-3.5 h-3.5" />
                智能组题
              </button>
              <button
                onClick={() => navigate('/contests')}
                className="flex items-center gap-1.5 px-3 py-1.5 text-sm text-gray-400 hover:text-gray-200 border border-gray-600 rounded-lg hover:bg-gray-700 transition"
              >
                <Trophy className="w-3.5 h-3.5" />
                比赛
              </button>
              <UserMenu />
            </div>
          )}
        </div>

        {/* 搜索栏 */}
        <div className="mb-6 flex gap-3">
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
            <input
              type="text"
              placeholder="按题号或标题搜索..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && loadProblems(1)}
              className="w-full pl-10 pr-4 py-2 border border-gray-600 rounded-lg bg-gray-800 text-sm text-gray-200 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <select
            value={difficulty}
            onChange={(e) => setDifficulty(e.target.value)}
            className="px-4 py-2 border border-gray-600 rounded-lg bg-gray-800 text-sm text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
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
            className="px-6 py-2 bg-indigo-600 text-white rounded-lg text-sm hover:bg-indigo-700 transition"
          >
            搜索
          </button>
        </div>

        {/* 题目列表表格 */}
        <div className="bg-gray-800 rounded-lg border border-gray-700 overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-800/50 border-b border-gray-700">
              <tr>
                <th className="px-4 py-3 text-center text-xs font-medium text-gray-400 uppercase w-12">状态</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">题号</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">标题</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">难度</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">通过率</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-700">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-gray-500">
                    加载中...
                  </td>
                </tr>
              ) : problems.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-gray-500">
                    暂无题目，请先同步题库
                  </td>
                </tr>
              ) : (
                problems.map((p) => {
                  const st = statusMap[p.slug]
                  return (
                  <tr
                    key={p.id}
                    className="hover:bg-gray-700/50 cursor-pointer transition"
                    onClick={() => handleClickProblem(p.slug)}
                  >
                    <td className="px-4 py-4 text-center">
                      {st === 'accepted' ? (
                        <CheckCircle2 className="w-4 h-4 text-emerald-400 mx-auto" />
                      ) : st === 'attempted' ? (
                        <MinusCircle className="w-4 h-4 text-amber-400 mx-auto" />
                      ) : null}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-300">{p.frontendId}</td>
                    <td className="px-6 py-4 text-sm font-medium text-gray-100">{p.title}</td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-1 text-xs rounded-full font-medium ${difficultyColor[p.difficulty] || 'text-gray-400 bg-gray-700'}`}>
                        {p.difficulty}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-400">
                      {p.acceptanceRate != null ? `${p.acceptanceRate}%` : '-'}
                    </td>
                    <td className="px-6 py-4">
                      <button
                        onClick={(e) => {
                          e.stopPropagation()
                          handleClickProblem(p.slug)
                        }}
                        className="text-sm text-indigo-400 hover:text-indigo-300"
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

        {/* 分页 */}
        {totalPages >= 1 && (
          <div className="mt-5 flex items-center justify-between">
            {/* 左侧：总数信息 */}
            <span className="text-sm text-gray-500">
              共 {total} 题
            </span>

            {/* 中间：页码按钮组 */}
            <div className="flex items-center gap-1">
              {/* 首页 */}
              <button
                disabled={currentPage <= 1}
                onClick={() => loadProblems(1)}
                className="p-1.5 rounded border border-gray-600 disabled:opacity-30 hover:bg-gray-700 transition"
                title="首页"
              >
                <ChevronsLeft className="w-4 h-4" />
              </button>
              {/* 上一页 */}
              <button
                disabled={currentPage <= 1}
                onClick={() => loadProblems(currentPage - 1)}
                className="p-1.5 rounded border border-gray-600 disabled:opacity-30 hover:bg-gray-700 transition"
                title="上一页"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>

              {/* 页码数字按钮 */}
              {(() => {
                const pages: (number | '...')[] = []
                const range = 2 // 当前页左右各显示几个
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
                    <span key={`dot-${idx}`} className="w-8 text-center text-gray-500 text-sm select-none">...</span>
                  ) : (
                    <button
                      key={p}
                      onClick={() => loadProblems(p)}
                      className={`w-8 h-8 text-sm rounded transition ${
                        p === currentPage
                          ? 'bg-indigo-600 text-white'
                          : 'border border-gray-600 hover:bg-gray-700 text-gray-300'
                      }`}
                    >
                      {p}
                    </button>
                  )
                )
              })()}

              {/* 下一页 */}
              <button
                disabled={currentPage >= totalPages}
                onClick={() => loadProblems(currentPage + 1)}
                className="p-1.5 rounded border border-gray-600 disabled:opacity-30 hover:bg-gray-700 transition"
                title="下一页"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
              {/* 末页 */}
              <button
                disabled={currentPage >= totalPages}
                onClick={() => loadProblems(totalPages)}
                className="p-1.5 rounded border border-gray-600 disabled:opacity-30 hover:bg-gray-700 transition"
                title="末页"
              >
                <ChevronsRight className="w-4 h-4" />
              </button>
            </div>

            {/* 右侧：每页条数选择 */}
            <div className="flex items-center gap-2 text-sm text-gray-400">
              <span>每页</span>
              <select
                value={pageSize}
                onChange={(e) => {
                  const size = Number(e.target.value)
                  setPageSize(size)
                  loadProblems(1, size)
                }}
                className="px-2 py-1 bg-gray-800 border border-gray-600 rounded text-gray-200 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
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

      {/* 历史练习轨迹 Drawer */}
      {chainsOpen && (
        <div className="fixed inset-0 z-50 flex justify-end">
          {/* 遮罩 */}
          <div
            className="absolute inset-0 bg-black/50"
            onClick={() => setChainsOpen(false)}
          />
          {/* 面板 */}
          <div className="relative w-80 h-full bg-gray-800 border-l border-gray-700 shadow-2xl flex flex-col animate-slide-in-right">
            {/* 标题栏 */}
            <div className="flex items-center justify-between px-4 py-3 border-b border-gray-700">
              <div className="flex items-center gap-2">
                <History className="w-4 h-4 text-indigo-400" />
                <span className="text-sm font-semibold text-gray-200">历史练习轨迹</span>
              </div>
              <button
                onClick={() => setChainsOpen(false)}
                className="p-1 rounded hover:bg-gray-700 text-gray-500 hover:text-gray-300 transition"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* 链列表 */}
            <div className="flex-1 overflow-y-auto">
              {chainsLoading ? (
                <div className="flex items-center justify-center py-12 text-gray-500">
                  <Loader2 className="w-5 h-5 animate-spin mr-2" />
                  加载中...
                </div>
              ) : chains.length === 0 ? (
                <div className="text-center py-12 text-gray-500 text-sm">
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
                      className="w-full text-left px-4 py-3 hover:bg-gray-700/50 transition border-b border-gray-700/50"
                    >
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-mono text-indigo-300">#{chain.headFrontendId}</span>
                        <span className="text-sm text-gray-200 truncate flex-1">{chain.headTitle}</span>
                        <span className={`text-[10px] px-1.5 py-0.5 rounded ${
                          chain.headDifficulty === 'Easy' ? 'bg-green-900/30 text-green-400' :
                          chain.headDifficulty === 'Medium' ? 'bg-yellow-900/30 text-yellow-400' :
                          chain.headDifficulty === 'Hard' ? 'bg-red-900/30 text-red-400' :
                          'bg-gray-700 text-gray-400'
                        }`}>
                          {chain.headDifficulty}
                        </span>
                      </div>
                      <div className="flex items-center gap-2 mt-1">
                        <span className="text-[11px] text-gray-500">
                          {chain.problemCount} 道题
                        </span>
                        <span className="text-[11px] text-gray-600">
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
