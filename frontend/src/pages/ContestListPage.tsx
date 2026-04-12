import { useEffect, useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Plus, Users, User, Trophy, Clock, Lock, Search,
  ChevronLeft, ChevronRight, Loader2, CalendarDays, Zap, Flag,
} from 'lucide-react'
import { getContests } from '@/api/contest'
import UserMenu from '@/components/UserMenu'
import type { ContestDetail } from '@/types'

/** 比赛状态配置 */
const statusConfig: Record<string, { label: string; color: string; dot: string; border: string }> = {
  draft:       { label: '草稿',   color: 'text-gray-400',   dot: 'bg-gray-500',   border: 'border-l-gray-500' },
  registering: { label: '报名中', color: 'text-yellow-400', dot: 'bg-yellow-400', border: 'border-l-yellow-400' },
  running:     { label: '进行中', color: 'text-green-400',  dot: 'bg-green-400',  border: 'border-l-green-400' },
  frozen:      { label: '已封榜', color: 'text-blue-400',   dot: 'bg-blue-400',   border: 'border-l-blue-400' },
  ended:       { label: '已结束', color: 'text-gray-400',   dot: 'bg-gray-500',   border: 'border-l-gray-600' },
  archived:    { label: '已归档', color: 'text-gray-500',   dot: 'bg-gray-600',   border: 'border-l-gray-700' },
}

const scoringLabel: Record<string, string> = {
  acm: 'ACM', oi: 'OI', cf: 'CF',
}

/** 状态筛选选项 */
const statusFilters = [
  { key: '',            label: '全部状态' },
  { key: 'running',     label: '进行中' },
  { key: 'registering', label: '报名中' },
  { key: 'ended',       label: '已结束' },
]

export default function ContestListPage() {
  const navigate = useNavigate()
  const [filter, setFilter] = useState('all')
  const [statusFilter, setStatusFilter] = useState('')
  const [keyword, setKeyword] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [contests, setContests] = useState<ContestDetail[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [loading, setLoading] = useState(false)

  // 倒计时
  const [now, setNow] = useState(Date.now())
  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(timer)
  }, [])

  useEffect(() => { loadContests() }, [filter, statusFilter, keyword, page])

  const loadContests = async () => {
    setLoading(true)
    try {
      const res = await getContests(filter, page, 20, keyword, statusFilter)
      if (res.code === 200) {
        setContests(res.data.records)
        setTotal(res.data.total)
      }
    } catch {}
    setLoading(false)
  }

  const handleSearch = () => {
    setKeyword(searchInput.trim())
    setPage(1)
  }

  /** 格式化日期 */
  const formatDate = (dt: string) =>
    new Date(dt).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })

  /** 格式化完整日期 */
  const formatFullDate = (dt: string) =>
    new Date(dt).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })

  /** 实时倒计时 */
  const getCountdown = (c: ContestDetail) => {
    const start = new Date(c.startTime).getTime()
    const end = new Date(c.endTime).getTime()

    if (c.status === 'running' || c.status === 'frozen') {
      const diff = end - now
      if (diff <= 0) return '即将结束'
      const h = Math.floor(diff / 3600000)
      const m = Math.floor((diff % 3600000) / 60000)
      const s = Math.floor((diff % 60000) / 1000)
      return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
    }
    if (c.status === 'registering') {
      const diff = start - now
      if (diff <= 0) return '即将开始'
      const d = Math.floor(diff / 86400000)
      const h = Math.floor((diff % 86400000) / 3600000)
      const m = Math.floor((diff % 3600000) / 60000)
      if (d > 0) return `${d}天${h}小时后开始`
      if (h > 0) return `${h}小时${m}分钟后开始`
      return `${m}分钟后开始`
    }
    return ''
  }

  /** 时间进度百分比 */
  const getTimeProgress = (c: ContestDetail) => {
    if (c.status !== 'running' && c.status !== 'frozen') return 0
    const start = new Date(c.startTime).getTime()
    const end = new Date(c.endTime).getTime()
    const progress = ((now - start) / (end - start)) * 100
    return Math.min(100, Math.max(0, progress))
  }

  const totalPages = Math.ceil(total / 20)

  // 统计（基于当前页数据的简单计数）
  const runningCount = contests.filter((c) => c.status === 'running' || c.status === 'frozen').length
  const registeringCount = contests.filter((c) => c.status === 'registering').length

  const renderContestCard = (c: ContestDetail) => {
    const st = statusConfig[c.status] || statusConfig.draft
    const countdown = getCountdown(c)
    const timeProgress = getTimeProgress(c)
    const isActive = c.status === 'running' || c.status === 'frozen'

    return (
      <div
        key={c.id}
        className={`bg-gray-800 rounded-lg border border-gray-700 border-l-[3px] ${st.border} hover:bg-gray-750 hover:border-gray-600 transition-all cursor-pointer group`}
        onClick={() => navigate(c.status === 'draft' && c.isCreator ? `/contests/${c.id}/edit` : `/contests/${c.id}`)}
      >
        <div className="p-4">
          {/* 第一行：标题 + 状态 + 倒计时 */}
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 mb-1.5">
                <h3 className="font-medium text-gray-100 truncate group-hover:text-white transition-colors">{c.title}</h3>
                <span className={`shrink-0 text-[11px] px-2 py-0.5 rounded-full font-medium flex items-center gap-1 ${st.color} bg-gray-700/80`}>
                  {(isActive) && <span className={`w-1.5 h-1.5 rounded-full ${st.dot} animate-pulse`} />}
                  {st.label}
                </span>
                {!c.isPublic && <Lock size={12} className="text-yellow-500 shrink-0" />}
              </div>

              {/* 标签行 */}
              <div className="flex flex-wrap items-center gap-2 text-[11px]">
                <span className="flex items-center gap-1 px-2 py-0.5 rounded bg-gray-700/60 text-gray-400">
                  {c.contestType === 'team' ? <Users size={11} /> : <User size={11} />}
                  {c.contestType === 'team' ? '组队赛' : '个人赛'}
                </span>
                <span className="px-2 py-0.5 rounded bg-gray-700/60 text-gray-400">
                  {scoringLabel[c.scoringRule]}
                </span>
                <span className="flex items-center gap-1 px-2 py-0.5 rounded bg-gray-700/60 text-gray-400">
                  <Clock size={11} /> {c.durationMinutes}分钟
                </span>
                <span className="px-2 py-0.5 rounded bg-gray-700/60 text-gray-400">
                  {c.problemCount} 题
                </span>
                <span className="flex items-center gap-1 px-2 py-0.5 rounded bg-gray-700/60 text-gray-400">
                  <Users size={11} /> {c.registeredCount}人报名
                </span>
              </div>
            </div>

            {/* 右侧时间 + 倒计时 */}
            <div className="text-right shrink-0">
              <div className="text-xs text-gray-500 flex items-center gap-1 justify-end">
                <CalendarDays size={12} />
                {formatDate(c.startTime)}
              </div>
              {countdown && (
                <div className={`text-sm font-mono mt-1 ${isActive ? 'text-green-400' : 'text-yellow-400'}`}>
                  {isActive && <Clock size={12} className="inline mr-1" />}
                  {countdown}
                </div>
              )}
            </div>
          </div>

          {/* 进度条：进行中比赛显示时间进度 */}
          {isActive && (
            <div className="mt-3">
              <div className="h-1 bg-gray-700 rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-green-500 to-green-400 rounded-full transition-all duration-1000"
                  style={{ width: `${timeProgress}%` }}
                />
              </div>
              <div className="flex justify-between mt-0.5 text-[10px] text-gray-600">
                <span>{formatDate(c.startTime)}</span>
                <span>{Math.round(timeProgress)}%</span>
                <span>{formatDate(c.endTime)}</span>
              </div>
            </div>
          )}

          {/* 底部信息 */}
          <div className="flex items-center gap-3 mt-3 text-xs text-gray-500">
            <span>创建者: {c.creatorName}</span>
            {c.contestType === 'team' && c.teamCount > 0 && <span>{c.teamCount} 支队伍</span>}
            <span className="flex-1" />
            {c.registered && (
              <span className="text-green-400 bg-green-900/20 px-2 py-0.5 rounded-full text-[11px] font-medium">
                已报名
              </span>
            )}
            {c.isCreator && (
              <span className="text-blue-400 bg-blue-900/20 px-2 py-0.5 rounded-full text-[11px] font-medium">
                我创建的
              </span>
            )}
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-900 text-gray-100">
      {/* 顶部导航 */}
      <header className="bg-gray-800 border-b border-gray-700 px-6 py-3 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate('/')} className="text-gray-400 hover:text-white text-sm">
            &larr; 返回题库
          </button>
          <h1 className="text-lg font-semibold flex items-center gap-2">
            <Trophy size={20} className="text-yellow-400" /> 比赛中心
          </h1>
        </div>
        <UserMenu />
      </header>

      <div className="max-w-5xl mx-auto p-6 space-y-5">

        {/* 搜索栏 + 创建按钮 */}
        <div className="flex items-center gap-3">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
            <input
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              className="w-full bg-gray-800 border border-gray-700 rounded-lg pl-10 pr-20 py-2.5 text-sm placeholder-gray-500 focus:border-gray-600 focus:outline-none transition-colors"
              placeholder="搜索比赛标题..."
            />
            {searchInput && (
              <button onClick={() => { setSearchInput(''); setKeyword(''); setPage(1) }}
                className="absolute right-16 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-300 text-xs">
                清除
              </button>
            )}
            <button onClick={handleSearch}
              className="absolute right-1 top-1/2 -translate-y-1/2 px-3 py-1.5 bg-blue-600 text-white rounded-md text-xs hover:bg-blue-700">
              搜索
            </button>
          </div>
          <button
            onClick={() => navigate('/contests/create')}
            className="flex items-center gap-1.5 px-4 py-2.5 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 shrink-0"
          >
            <Plus size={16} /> 创建比赛
          </button>
        </div>

        {/* 筛选栏：范围 + 状态 */}
        <div className="flex items-center justify-between gap-4">
          {/* 范围筛选 */}
          <div className="flex gap-1.5">
            {[
              { key: 'all', label: '所有比赛' },
              { key: 'my_joined', label: '我参加的' },
              { key: 'my_created', label: '我创建的' },
            ].map((f) => (
              <button
                key={f.key}
                onClick={() => { setFilter(f.key); setPage(1) }}
                className={`px-3 py-1.5 rounded-lg text-sm transition-colors ${
                  filter === f.key
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-800 text-gray-400 hover:text-white border border-gray-700'
                }`}
              >
                {f.label}
              </button>
            ))}
          </div>

          {/* 状态筛选 */}
          <div className="flex gap-1">
            {statusFilters.map((sf) => (
              <button
                key={sf.key}
                onClick={() => { setStatusFilter(sf.key); setPage(1) }}
                className={`px-2.5 py-1 rounded text-xs transition-colors ${
                  statusFilter === sf.key
                    ? 'bg-gray-700 text-white'
                    : 'text-gray-500 hover:text-gray-300'
                }`}
              >
                {sf.key && <span className={`inline-block w-1.5 h-1.5 rounded-full mr-1 ${
                  sf.key === 'running' ? 'bg-green-400' : sf.key === 'registering' ? 'bg-yellow-400' : 'bg-gray-500'
                }`} />}
                {sf.label}
              </button>
            ))}
          </div>
        </div>

        {/* 结果信息 */}
        <div className="flex items-center justify-between text-xs text-gray-500">
          <span>
            共 {total} 场比赛
            {keyword && <span className="ml-1">· 搜索「{keyword}」</span>}
          </span>
          {runningCount > 0 && (
            <span className="flex items-center gap-1 text-green-400">
              <Zap size={12} /> {runningCount} 场进行中
            </span>
          )}
        </div>

        {/* 比赛列表 */}
        {loading ? (
          <div className="flex items-center justify-center py-20 text-gray-500">
            <Loader2 className="animate-spin mr-2" size={18} /> 加载中...
          </div>
        ) : contests.length === 0 ? (
          <div className="text-center py-20">
            <div className="text-5xl opacity-10 mb-4">🏆</div>
            <p className="text-gray-500 text-sm">
              {keyword ? `没有找到与「${keyword}」相关的比赛` : '暂无比赛'}
            </p>
            {keyword && (
              <button onClick={() => { setSearchInput(''); setKeyword(''); setPage(1) }}
                className="mt-2 text-blue-400 text-sm hover:text-blue-300">清除搜索</button>
            )}
          </div>
        ) : (
          <div className="space-y-3">
            {contests.map(renderContestCard)}
          </div>
        )}

        {/* 分页 */}
        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 pt-2">
            <button onClick={() => setPage(1)} disabled={page <= 1}
              className="text-xs text-gray-400 hover:text-white disabled:opacity-30">首页</button>
            <button
              onClick={() => setPage(Math.max(1, page - 1))}
              disabled={page === 1}
              className="p-1 text-gray-400 hover:text-white disabled:opacity-30"
            >
              <ChevronLeft size={16} />
            </button>
            <input
              type="number"
              value={page}
              min={1}
              max={totalPages}
              onChange={(e) => {
                const p = Number(e.target.value)
                if (p >= 1 && p <= totalPages) setPage(p)
              }}
              className="w-12 bg-gray-800 border border-gray-700 rounded px-1 py-0.5 text-xs text-center [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
            />
            <span className="text-xs text-gray-500">/ {totalPages}</span>
            <button
              onClick={() => setPage(Math.min(totalPages, page + 1))}
              disabled={page === totalPages}
              className="p-1 text-gray-400 hover:text-white disabled:opacity-30"
            >
              <ChevronRight size={16} />
            </button>
            <button onClick={() => setPage(totalPages)} disabled={page >= totalPages}
              className="text-xs text-gray-400 hover:text-white disabled:opacity-30">末页</button>
          </div>
        )}
      </div>
    </div>
  )
}
