import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Clock,
  Loader2,
  Lock,
  Plus,
  Search,
  Trophy,
  User,
  Users,
  Zap,
} from 'lucide-react'
import { getContests } from '@/api/contest'
import UserMenu from '@/components/UserMenu'
import ThemeToggle from '@/components/ThemeToggle'
import ContestSidebar from '@/components/ContestSidebar'
import type { ContestDetail } from '@/types'

const statusConfig: Record<string, { label: string; color: string; dot: string; border: string }> = {
  draft: { label: '草稿', color: 'theme-faint', dot: 'bg-gray-500', border: 'border-l-gray-500' },
  registering: { label: '报名中', color: 'text-[var(--warning)]', dot: 'bg-[var(--warning)]', border: 'border-l-[var(--warning)]' },
  running: { label: '进行中', color: 'text-[var(--success)]', dot: 'bg-[var(--success)]', border: 'border-l-[var(--success)]' },
  frozen: { label: '已封榜', color: 'text-[var(--info)]', dot: 'bg-[var(--info)]', border: 'border-l-[var(--info)]' },
  ended: { label: '已结束', color: 'theme-faint', dot: 'bg-gray-500', border: 'border-l-gray-500' },
  archived: { label: '已归档', color: 'theme-hint', dot: 'bg-gray-600', border: 'border-l-gray-600' },
}

const scoringLabel: Record<string, string> = { acm: 'ACM', oi: 'OI', cf: 'CF' }

const statusFilters = [
  { key: '', label: '全部状态' },
  { key: 'running', label: '进行中' },
  { key: 'registering', label: '报名中' },
  { key: 'ended', label: '已结束' },
  { key: 'draft', label: '草稿' },
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
  const [now, setNow] = useState(Date.now())

  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(timer)
  }, [])

  useEffect(() => { void loadContests() }, [filter, statusFilter, keyword, page])

  const loadContests = async () => {
    setLoading(true)
    try {
      const res = await getContests(filter, page, 20, keyword, statusFilter)
      if (res.code === 200) { setContests(res.data.records); setTotal(res.data.total) }
    } catch {}
    setLoading(false)
  }

  const handleSearch = () => { setKeyword(searchInput.trim()); setPage(1) }

  const formatDate = (v: string) =>
    new Date(v).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })

  const getCountdown = (c: ContestDetail) => {
    const start = new Date(c.startTime).getTime()
    const end = new Date(c.endTime).getTime()
    if (c.status === 'running' || c.status === 'frozen') {
      const diff = end - now
      if (diff <= 0) return '即将结束'
      const h = Math.floor(diff / 3600000); const m = Math.floor((diff % 3600000) / 60000); const s = Math.floor((diff % 60000) / 1000)
      return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
    }
    if (c.status === 'registering') {
      const diff = start - now
      if (diff <= 0) return '即将开始'
      const d = Math.floor(diff / 86400000); const h = Math.floor((diff % 86400000) / 3600000); const m = Math.floor((diff % 3600000) / 60000)
      if (d > 0) return `${d}天${h}小时后`
      if (h > 0) return `${h}小时${m}分钟后`
      return `${m}分钟后`
    }
    return ''
  }

  const getTimeProgress = (c: ContestDetail) => {
    if (c.status !== 'running' && c.status !== 'frozen') return 0
    const start = new Date(c.startTime).getTime(); const end = new Date(c.endTime).getTime()
    return Math.min(100, Math.max(0, ((now - start) / (end - start)) * 100))
  }

  const totalPages = Math.ceil(total / 20)
  const stats = useMemo(() => ({
    running: contests.filter(c => c.status === 'running' || c.status === 'frozen').length,
    registering: contests.filter(c => c.status === 'registering').length,
  }), [contests])

  const renderContestCard = (contest: ContestDetail) => {
    const status = statusConfig[contest.status] || statusConfig.draft
    const countdown = getCountdown(contest)
    const progress = getTimeProgress(contest)
    const isActive = contest.status === 'running' || contest.status === 'frozen'
    const isTeam = contest.contestType === 'team'

    return (
      <div
        key={contest.id}
        className={`theme-card rounded-2xl border-l-[3px] ${status.border} theme-hover cursor-pointer group transition-all`}
        onClick={() => navigate(contest.status === 'draft' && contest.isCreator ? `/contests/${contest.id}/edit` : `/contests/${contest.id}`)}
      >
        <div className="p-4">
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1 min-w-0 space-y-2">
              <div className="flex items-center gap-2 flex-wrap">
                <h3 className="font-medium theme-text truncate group-hover:text-[var(--accent)] transition-colors">{contest.title}</h3>
                <span className={`shrink-0 text-[11px] px-2 py-0.5 rounded-full font-medium flex items-center gap-1 ${status.color} theme-tag`}>
                  {isActive && <span className={`w-1.5 h-1.5 rounded-full ${status.dot} animate-pulse`} />}
                  {status.label}
                </span>
                {!contest.isPublic && <Lock size={12} style={{ color: 'var(--warning)' }} className="shrink-0" />}
                {contest.registered && <span className="theme-status-accepted px-2 py-0.5 rounded-full text-[11px] font-medium">已报名</span>}
                {contest.isCreator && <span className="theme-status-info px-2 py-0.5 rounded-full text-[11px] font-medium">我创建的</span>}
              </div>

              <div className="flex flex-wrap items-center gap-2 text-[11px]">
                {[
                  { icon: isTeam ? <Users size={11} /> : <User size={11} />, text: isTeam ? '组队赛' : '个人赛' },
                  { text: scoringLabel[contest.scoringRule] },
                  { icon: <Clock size={11} />, text: `${contest.durationMinutes}分钟` },
                  { text: `${contest.problemCount}题` },
                  { icon: <Users size={11} />, text: `${contest.registeredCount}${isTeam ? '队' : '人'}` },
                ].map((item, i) => (
                  <span key={i} className="flex items-center gap-1 px-2 py-0.5 rounded-lg theme-tag">{item.icon}{item.text}</span>
                ))}
              </div>
            </div>

            <div className="text-right shrink-0 min-w-[120px]">
              <div className="text-xs theme-faint flex items-center gap-1 justify-end"><CalendarDays size={12} />{formatDate(contest.startTime)}</div>
              {countdown && (
                <div className={`text-sm font-mono mt-1 ${isActive ? 'text-[var(--success)]' : 'text-[var(--warning)]'}`}>
                  {isActive && <Clock size={12} className="inline mr-1" />}{countdown}
                </div>
              )}
            </div>
          </div>

          {isActive && (
            <div className="mt-3">
              <div className="h-1 rounded-full overflow-hidden" style={{ background: 'var(--hover-bg)' }}>
                <div className="h-full rounded-full transition-all duration-1000" style={{ width: `${progress}%`, background: 'linear-gradient(90deg, var(--success), var(--accent))' }} />
              </div>
              <div className="flex justify-between mt-0.5 text-[10px] theme-hint">
                <span>{formatDate(contest.startTime)}</span>
                <span>{Math.round(progress)}%</span>
                <span>{formatDate(contest.endTime)}</span>
              </div>
            </div>
          )}

          <div className="flex items-center mt-2 text-xs theme-faint">
            <span>创建者：{contest.creatorName}</span>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="h-screen flex flex-col theme-bg-gradient overflow-hidden">
      <header className="theme-header px-6 py-3 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate('/')} className="theme-button-ghost text-sm">&larr; 返回题库</button>
          <h1 className="text-lg font-semibold flex items-center gap-2 theme-text">
            <Trophy size={20} style={{ color: 'var(--warning)' }} />
            比赛中心
          </h1>
        </div>
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/contests/create')} className="px-3 py-1.5 theme-button-blue rounded-lg text-sm inline-flex items-center gap-1.5">
            <Plus size={14} />创建比赛
          </button>
          <ThemeToggle />
          <UserMenu />
        </div>
      </header>

      <div className="flex flex-1 overflow-hidden">
        <ContestSidebar />

        <main className="flex-1 overflow-y-auto p-6 space-y-4">
          {/* 搜索和筛选 */}
          <div className="flex flex-col lg:flex-row gap-3">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 theme-hint" />
              <input
                value={searchInput} onChange={(e) => setSearchInput(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                className="w-full theme-input rounded-xl pl-10 pr-24 py-2.5 text-sm" placeholder="搜索比赛标题"
              />
              {searchInput && (
                <button onClick={() => { setSearchInput(''); setKeyword(''); setPage(1) }}
                  className="absolute right-16 top-1/2 -translate-y-1/2 theme-hint text-xs hover:text-[var(--text-primary)]">清除</button>
              )}
              <button onClick={handleSearch} className="absolute right-1 top-1/2 -translate-y-1/2 px-3 py-1.5 theme-button-blue rounded-lg text-xs">搜索</button>
            </div>

            <div className="flex gap-1.5 flex-wrap">
              {[{ key: 'all', label: '全部' }, { key: 'my_joined', label: '我参加的' }, { key: 'my_created', label: '我创建的' }].map((item) => (
                <button key={item.key} onClick={() => { setFilter(item.key); setPage(1) }}
                  className={`px-3 py-2 rounded-xl text-sm transition ${filter === item.key ? 'theme-button-blue' : 'theme-button-secondary'}`}>{item.label}</button>
              ))}
            </div>
          </div>

          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
            <div className="flex gap-2 flex-wrap">
              {statusFilters.map((item) => (
                <button key={item.key} onClick={() => { setStatusFilter(item.key); setPage(1) }}
                  className={`px-2.5 py-1.5 rounded-lg text-xs transition ${statusFilter === item.key ? 'theme-chip' : 'theme-button-ghost'}`}>{item.label}</button>
              ))}
            </div>
            <div className="flex items-center gap-3 text-xs theme-faint">
              <span>共 {total} 场{keyword && ` · 搜索"${keyword}"`}</span>
              {stats.running > 0 && <span className="flex items-center gap-1" style={{ color: 'var(--success)' }}><Zap size={12} />{stats.running} 场进行中</span>}
            </div>
          </div>

          {/* 比赛列表 */}
          {loading ? (
            <div className="flex items-center justify-center py-20 theme-faint"><Loader2 className="animate-spin mr-2" size={18} />加载中...</div>
          ) : contests.length === 0 ? (
            <div className="text-center py-20">
              <div className="text-5xl opacity-10 mb-4">&#127942;</div>
              <p className="theme-faint text-sm">{keyword ? `没有找到与"${keyword}"相关的比赛` : '暂无比赛'}</p>
            </div>
          ) : (
            <div className="space-y-3">{contests.map(renderContestCard)}</div>
          )}

          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 pt-2">
              <button onClick={() => setPage(1)} disabled={page <= 1} className="text-xs theme-button-ghost disabled:opacity-30">首页</button>
              <button onClick={() => setPage(Math.max(1, page - 1))} disabled={page === 1} className="p-1 theme-button-ghost disabled:opacity-30"><ChevronLeft size={16} /></button>
              <input type="number" value={page} min={1} max={totalPages}
                onChange={(e) => { const p = Number(e.target.value); if (p >= 1 && p <= totalPages) setPage(p) }}
                className="w-12 theme-input rounded px-1 py-0.5 text-xs text-center [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none" />
              <span className="text-xs theme-faint">/ {totalPages}</span>
              <button onClick={() => setPage(Math.min(totalPages, page + 1))} disabled={page === totalPages} className="p-1 theme-button-ghost disabled:opacity-30"><ChevronRight size={16} /></button>
              <button onClick={() => setPage(totalPages)} disabled={page >= totalPages} className="text-xs theme-button-ghost disabled:opacity-30">末页</button>
            </div>
          )}
        </main>
      </div>
    </div>
  )
}
