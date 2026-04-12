import { useEffect, useState, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Clock, Users, User, Trophy, Lock, Unlock, Send, Loader2, ArrowLeft, PanelLeftClose, PanelLeftOpen, ChevronDown, RotateCcw } from 'lucide-react'
import Editor from '@monaco-editor/react'
import { toast, confirm } from '@/store/uiStore'
import {
  getContestDetail, registerContest, cancelRegistration, publishContest,
  getContestProblems, contestSubmit, pollContestResult, getMyContestSubmissions,
  getStandings, createTeam, joinTeam, getTeams, unfreezeStandings,
} from '@/api/contest'
import { getProblemBySlug } from '@/api/problem'
import UserMenu from '@/components/UserMenu'
import type { ContestDetail, ProblemSetItemDetail, ContestSubmission, StandingData, ContestTeam, Problem } from '@/types'

const diffColor: Record<string, string> = {
  Easy: 'text-green-400', EASY: 'text-green-400',
  Medium: 'text-yellow-400', MEDIUM: 'text-yellow-400',
  Hard: 'text-red-400', HARD: 'text-red-400',
}
const diffBg: Record<string, string> = {
  Easy: 'bg-green-900/30', EASY: 'bg-green-900/30',
  Medium: 'bg-yellow-900/30', MEDIUM: 'bg-yellow-900/30',
  Hard: 'bg-red-900/30', HARD: 'bg-red-900/30',
}

export default function ContestDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const contestId = Number(id)

  const [contest, setContest] = useState<ContestDetail | null>(null)
  const [problems, setProblems] = useState<ProblemSetItemDetail[]>([])
  const [standings, setStandings] = useState<StandingData | null>(null)
  const [mySubmissions, setMySubmissions] = useState<ContestSubmission[]>([])
  const [teams, setTeams] = useState<ContestTeam[]>([])
  const [selectedProblem, setSelectedProblem] = useState<ProblemSetItemDetail | null>(null)
  const [problemDetail, setProblemDetail] = useState<Problem | null>(null)
  const [code, setCode] = useState('')
  const [language, setLanguage] = useState('java')
  const [submitting, setSubmitting] = useState(false)
  const [tab, setTab] = useState<'problems' | 'standings' | 'submissions' | 'teams'>('problems')
  const [loading, setLoading] = useState(true)
  const [password, setPassword] = useState('')
  const [teamName, setTeamName] = useState('')
  const [inviteCode, setInviteCode] = useState('')

  // 三栏布局状态
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const [descWidth, setDescWidth] = useState(45) // 题目描述区占比（%），剩余给编辑器
  const [dragging, setDragging] = useState(false)
  const containerRef = useRef<HTMLDivElement>(null)

  // 倒计时
  const [countdown, setCountdown] = useState('')

  useEffect(() => { loadContest() }, [contestId])

  // 倒计时定时器
  useEffect(() => {
    if (!contest || !['running', 'frozen'].includes(contest.status)) return
    const timer = setInterval(() => {
      const now = new Date().getTime()
      const end = new Date(contest.endTime).getTime()
      const diff = end - now
      if (diff <= 0) { setCountdown('00:00:00'); clearInterval(timer); return }
      const h = Math.floor(diff / 3600000)
      const m = Math.floor((diff % 3600000) / 60000)
      const s = Math.floor((diff % 60000) / 1000)
      setCountdown(`${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`)
    }, 1000)
    return () => clearInterval(timer)
  }, [contest])

  const loadContest = async () => {
    setLoading(true)
    try {
      const res = await getContestDetail(contestId)
      if (res.code === 200) {
        const c = res.data
        // 草稿态且是创建者 → 重定向到编辑页
        if (c.status === 'draft' && c.isCreator) {
          navigate(`/contests/${contestId}/edit`, { replace: true })
          return
        }
        setContest(c)
        const canView = ['running', 'frozen', 'ended', 'archived'].includes(c.status) || c.isCreator
        if (canView) { loadProblems(); loadStandings() }
        if (c.registered) loadMySubmissions()
        if (c.contestType === 'team') loadTeams()
      }
    } catch {}
    setLoading(false)
  }

  const loadProblems = async () => {
    try { const res = await getContestProblems(contestId); if (res.code === 200) setProblems(res.data) } catch {}
  }
  const loadStandings = async () => {
    try { const res = await getStandings(contestId); if (res.code === 200) setStandings(res.data) } catch {}
  }
  const loadMySubmissions = async () => {
    try { const res = await getMyContestSubmissions(contestId); if (res.code === 200) setMySubmissions(res.data) } catch {}
  }
  const loadTeams = async () => {
    try { const res = await getTeams(contestId); if (res.code === 200) setTeams(res.data) } catch {}
  }

  const handleRegister = async () => { try { await registerContest(contestId, password || undefined); loadContest() } catch (e: any) { toast.error(e.message) } }
  const handleCancelReg = async () => { try { await cancelRegistration(contestId); loadContest() } catch (e: any) { toast.error(e.message) } }
  const handlePublish = async () => { if (!await confirm('确定发布比赛？', { type: 'warning', confirmText: '发布' })) return; try { await publishContest(contestId); loadContest() } catch (e: any) { toast.error(e.message) } }
  const handleUnfreeze = async () => { try { await unfreezeStandings(contestId); loadContest(); loadStandings() } catch (e: any) { toast.error(e.message) } }

  // 语言映射：提交语言 → Monaco 语言 ID
  const monacoLangMap: Record<string, string> = {
    java: 'java', python3: 'python', cpp: 'cpp', c: 'c', javascript: 'javascript', typescript: 'typescript', go: 'go', rust: 'rust',
  }
  // 语言映射：提交语言 → codeSnippets 的 langSlug
  const langSlugMap: Record<string, string[]> = {
    java: ['java'], python3: ['python3', 'python'], cpp: ['cpp', 'c++'],
  }

  const handleSelectProblem = async (item: ProblemSetItemDetail) => {
    setSelectedProblem(item)
    try {
      const res = await getProblemBySlug(item.slug, contest?.ojPlatform || 'leetcode')
      if (res.code === 200) {
        setProblemDetail(res.data)
        // 加载初始代码模板
        const snippets = res.data.codeSnippets
        if (snippets && snippets.length > 0) {
          const slugs = langSlugMap[language] || [language]
          const matched = snippets.find((s) => slugs.includes(s.langSlug))
          setCode(matched ? matched.code : snippets[0].code)
        } else {
          setCode('')
        }
      }
    } catch {}
  }

  const handleSubmit = async () => {
    if (!selectedProblem || !code.trim()) return
    setSubmitting(true)
    try {
      const res = await contestSubmit(contestId, { problemSlug: selectedProblem.slug, language, code, ojPlatform: contest?.ojPlatform })
      if (res.code === 200) {
        const subId = res.data.id
        const poll = setInterval(async () => {
          try {
            const r = await pollContestResult(contestId, subId)
            if (r.code === 200 && r.data.status !== 'Pending') { clearInterval(poll); setSubmitting(false); loadMySubmissions(); loadStandings() }
          } catch { clearInterval(poll); setSubmitting(false) }
        }, 2000)
      }
    } catch (e: any) { toast.error(e.message); setSubmitting(false) }
  }

  const handleCreateTeam = async () => {
    if (!teamName.trim()) return
    try { const res = await createTeam(contestId, teamName); if (res.code === 200) { toast.success(`队伍创建成功！邀请码：${res.data.inviteCode}`, 6000); loadTeams(); loadContest() } } catch (e: any) { toast.error(e.message) }
  }
  const handleJoinTeam = async () => {
    if (!inviteCode.trim()) return
    try { await joinTeam(contestId, inviteCode); toast.success('加入成功！'); loadTeams(); loadContest() } catch (e: any) { toast.error(e.message) }
  }

  const formatPenalty = (seconds: number) => {
    const h = Math.floor(seconds / 3600)
    const m = Math.floor((seconds % 3600) / 60)
    return `${h}:${m.toString().padStart(2, '0')}`
  }

  // 拖拽调整中间分割线
  const handleDragStart = () => setDragging(true)
  useEffect(() => {
    if (!dragging) return
    const handleMove = (e: MouseEvent) => {
      if (!containerRef.current) return
      const rect = containerRef.current.getBoundingClientRect()
      const sidebarW = sidebarOpen ? 220 : 0
      const usable = rect.width - sidebarW
      const x = e.clientX - rect.left - sidebarW
      const pct = Math.min(70, Math.max(25, (x / usable) * 100))
      setDescWidth(pct)
    }
    const handleUp = () => setDragging(false)
    window.addEventListener('mousemove', handleMove)
    window.addEventListener('mouseup', handleUp)
    return () => { window.removeEventListener('mousemove', handleMove); window.removeEventListener('mouseup', handleUp) }
  }, [dragging, sidebarOpen])

  if (loading) return <div className="min-h-screen bg-gray-900 flex items-center justify-center text-gray-500"><Loader2 className="animate-spin mr-2" /> 加载中...</div>
  if (!contest) return <div className="min-h-screen bg-gray-900 flex items-center justify-center text-gray-500">比赛不存在</div>

  const isActive = ['running', 'frozen'].includes(contest.status)

  return (
    <div className="h-screen flex flex-col bg-gray-900 text-gray-100 overflow-hidden">
      {/* ====== 顶部导航 ====== */}
      <header className="bg-gray-800 border-b border-gray-700 px-4 py-2 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/contests')} className="text-gray-400 hover:text-white"><ArrowLeft size={16} /></button>
          <h1 className="font-semibold text-sm">{contest.title}</h1>
          <span className={`text-xs px-2 py-0.5 rounded ${
            isActive ? 'bg-green-900/30 text-green-400' :
            contest.status === 'frozen' ? 'bg-blue-900/30 text-blue-400' :
            'bg-gray-700 text-gray-400'
          }`}>
            {contest.status === 'running' ? '进行中' : contest.status === 'frozen' ? '已封榜' :
             contest.status === 'registering' ? '报名中' : contest.status === 'ended' ? '已结束' : contest.status}
          </span>
          {isActive && countdown && (
            <span className="text-sm font-mono text-green-400 flex items-center gap-1"><Clock size={13} />{countdown}</span>
          )}
        </div>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-3 text-xs text-gray-500">
            <span>{contest.contestType === 'team' ? '组队赛' : '个人赛'}</span>
            <span>{contest.scoringRule === 'acm' ? 'ACM' : contest.scoringRule === 'oi' ? 'OI' : 'CF'}</span>
            <span>{contest.registeredCount}人</span>
          </div>
          {/* 操作按钮 */}
          {contest.isCreator && contest.status === 'draft' && (
            <button onClick={handlePublish} className="px-2 py-1 bg-blue-600 text-white rounded text-xs hover:bg-blue-700">发布</button>
          )}
          {contest.isCreator && (contest.status === 'frozen' || contest.status === 'ended') && (
            <button onClick={handleUnfreeze} className="px-2 py-1 bg-yellow-600 text-white rounded text-xs hover:bg-yellow-700 flex items-center gap-1"><Unlock size={12} />解封</button>
          )}
          {!contest.registered && (contest.status === 'registering' || contest.status === 'running') && (
            <button onClick={handleRegister} className="px-2 py-1 bg-green-600 text-white rounded text-xs hover:bg-green-700">报名</button>
          )}
          <UserMenu />
        </div>
      </header>

      {/* ====== Tab 栏 ====== */}
      <div className="flex gap-0.5 px-4 bg-gray-800 border-b border-gray-700 shrink-0">
        {[
          { key: 'problems' as const, label: '题目' },
          { key: 'standings' as const, label: '榜单' },
          { key: 'submissions' as const, label: '我的提交' },
          ...(contest.contestType === 'team' ? [{ key: 'teams' as const, label: '队伍' }] : []),
        ].map((t) => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`px-4 py-2 text-xs font-medium border-b-2 transition-colors ${
              tab === t.key ? 'border-blue-500 text-blue-400' : 'border-transparent text-gray-500 hover:text-gray-300'
            }`}>{t.label}</button>
        ))}
      </div>

      {/* ====== 主体内容 ====== */}
      <div className="flex-1 overflow-hidden">

        {/* ====== 题目 Tab ====== */}
        {tab === 'problems' && (
          // 草稿态 / 报名态：显示管理面板或等待提示
          ['draft', 'registering'].includes(contest.status) && problems.length === 0 ? (
            <div className="h-full flex items-center justify-center">
              <div className="max-w-md text-center space-y-4">
                {contest.isCreator ? (
                  <>
                    <div className="text-5xl opacity-15">
                      {contest.status === 'draft' ? '📝' : '📢'}
                    </div>
                    <h2 className="text-lg font-medium">
                      {contest.status === 'draft' ? '比赛草稿' : '报名进行中'}
                    </h2>
                    <div className="bg-gray-800 rounded-lg border border-gray-700 p-4 text-left text-sm space-y-2">
                      <div className="flex justify-between">
                        <span className="text-gray-500">比赛类型</span>
                        <span>{contest.contestType === 'team' ? '组队赛' : '个人赛'}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">计分规则</span>
                        <span>{contest.scoringRule === 'acm' ? 'ACM 罚时制' : contest.scoringRule === 'oi' ? 'OI 分数制' : 'CF 风格'}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">开始时间</span>
                        <span>{new Date(contest.startTime).toLocaleString('zh-CN')}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">时长</span>
                        <span>{contest.durationMinutes} 分钟</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">题目</span>
                        <span className={contest.problemCount === 0 ? 'text-red-400' : 'text-green-400'}>
                          {contest.problemCount === 0 ? '未设置题目' : `${contest.problemCount} 题`}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">报名人数</span>
                        <span>{contest.registeredCount} 人</span>
                      </div>
                    </div>
                    {contest.status === 'draft' && (
                      <div className="space-y-2">
                        {contest.problemCount === 0 && (
                          <p className="text-xs text-yellow-400">请先在创建比赛页面设置题目，才能发布比赛</p>
                        )}
                        <button onClick={handlePublish} disabled={contest.problemCount === 0}
                          className="px-5 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-40 disabled:cursor-not-allowed">
                          发布比赛
                        </button>
                      </div>
                    )}
                    {contest.status === 'registering' && (
                      <p className="text-xs text-gray-500">
                        比赛将于 {new Date(contest.startTime).toLocaleString('zh-CN')} 自动开始，届时题目将对参赛者可见
                      </p>
                    )}
                  </>
                ) : (
                  <>
                    <div className="text-5xl opacity-15">
                      {contest.status === 'draft' ? '🔒' : '⏳'}
                    </div>
                    <h2 className="text-lg font-medium">
                      {contest.status === 'draft' ? '比赛尚未发布' : '比赛未开始'}
                    </h2>
                    <p className="text-sm text-gray-500">
                      {contest.status === 'draft'
                        ? '该比赛正在筹备中，请等待创建者发布'
                        : `比赛将于 ${new Date(contest.startTime).toLocaleString('zh-CN')} 开始，届时题目可见`}
                    </p>
                    {contest.status === 'registering' && !contest.registered && (
                      <div className="flex items-center justify-center gap-2">
                        {!contest.isPublic && (
                          <input value={password} onChange={(e) => setPassword(e.target.value)} placeholder="比赛密码" type="password"
                            className="bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm w-32" />
                        )}
                        <button onClick={handleRegister} className="px-4 py-1.5 bg-green-600 text-white rounded text-sm hover:bg-green-700">
                          立即报名
                        </button>
                      </div>
                    )}
                    {contest.registered && (
                      <p className="text-xs text-green-400">你已报名，等待比赛开始</p>
                    )}
                  </>
                )}
              </div>
            </div>
          ) : (
            // 进行中 / 已结束：三栏做题布局
            <div ref={containerRef} className="flex h-full" style={{ userSelect: dragging ? 'none' : 'auto' }}>
            {/* 左侧：题目列表（可收放） */}
            <div className={`shrink-0 border-r border-gray-700 bg-gray-850 transition-all duration-200 flex flex-col ${sidebarOpen ? 'w-[220px]' : 'w-0'}`}
              style={{ overflow: 'hidden' }}>
              <div className="px-3 py-2 border-b border-gray-700 flex items-center justify-between">
                <span className="text-xs font-medium text-gray-400">题目列表</span>
                <button onClick={() => setSidebarOpen(false)} className="text-gray-500 hover:text-white">
                  <PanelLeftClose size={14} />
                </button>
              </div>
              <div className="flex-1 overflow-y-auto py-1">
                {problems.length === 0 ? (
                  <p className="text-gray-500 text-xs py-6 text-center">
                    {['running', 'frozen', 'ended'].includes(contest.status) ? '暂无题目' : '比赛未开始'}
                  </p>
                ) : problems.map((p, i) => {
                  const mySubs = mySubmissions.filter((s) => s.problemId === p.problemId)
                  const isAc = mySubs.some((s) => s.status === 'Accepted')
                  const attempts = mySubs.length
                  const isSelected = selectedProblem?.id === p.id
                  return (
                    <div key={p.id} onClick={() => handleSelectProblem(p)}
                      className={`flex items-center gap-2 px-3 py-2 cursor-pointer text-xs transition-colors ${
                        isSelected ? 'bg-gray-700 border-l-2 border-blue-500' : 'hover:bg-gray-800 border-l-2 border-transparent'
                      }`}>
                      <span className={`w-5 h-5 flex items-center justify-center rounded text-xs font-bold shrink-0 ${
                        isAc ? 'bg-green-900/40 text-green-400' : attempts > 0 ? 'bg-red-900/30 text-red-400' : 'bg-gray-700 text-gray-500'
                      }`}>{String.fromCharCode(65 + i)}</span>
                      <div className="flex-1 min-w-0">
                        <div className="truncate text-gray-200">{p.title}</div>
                        <div className="flex items-center gap-1.5 mt-0.5">
                          <span className={`text-[10px] ${diffColor[p.difficulty] || 'text-gray-500'}`}>{p.difficulty}</span>
                          {isAc && <span className="text-[10px] text-green-400">AC</span>}
                          {attempts > 0 && !isAc && <span className="text-[10px] text-red-400">{attempts}次</span>}
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>

            {/* 收起时的展开按钮 */}
            {!sidebarOpen && (
              <button onClick={() => setSidebarOpen(true)}
                className="shrink-0 w-6 bg-gray-800 border-r border-gray-700 flex items-center justify-center hover:bg-gray-700 text-gray-500 hover:text-white">
                <PanelLeftOpen size={14} />
              </button>
            )}

            {/* 中间+右侧：题目描述 + 代码编辑器 */}
            {selectedProblem && problemDetail ? (
              <>
                {/* 中间：题目描述 */}
                <div className="flex flex-col overflow-hidden" style={{ width: `${descWidth}%` }}>
                  <div className="px-4 py-2 border-b border-gray-700 bg-gray-800 flex items-center gap-2 shrink-0">
                    <span className="text-xs font-bold text-gray-400">
                      {String.fromCharCode(65 + problems.findIndex((p) => p.id === selectedProblem.id))}
                    </span>
                    <span className="text-sm font-medium">{problemDetail.frontendId}. {problemDetail.title}</span>
                    <span className={`text-xs px-1.5 py-0.5 rounded ${diffColor[problemDetail.difficulty] || ''} ${diffBg[problemDetail.difficulty] || ''}`}>
                      {problemDetail.difficulty}
                    </span>
                  </div>
                  <div className="flex-1 overflow-y-auto p-4">
                    <div className="prose prose-invert prose-sm max-w-none"
                      dangerouslySetInnerHTML={{ __html: problemDetail.contentMarkdown || '' }} />
                  </div>
                </div>

                {/* 拖拽分割线 */}
                <div onMouseDown={handleDragStart}
                  className={`w-1 shrink-0 cursor-col-resize transition-colors ${dragging ? 'bg-blue-500' : 'bg-gray-700 hover:bg-gray-600'}`} />

                {/* 右侧：代码编辑器 */}
                <div className="flex-1 flex flex-col overflow-hidden min-w-0">
                  <div className="px-4 py-2 border-b border-gray-700 bg-gray-800 flex items-center justify-between shrink-0">
                    <span className="text-xs text-gray-400">代码编辑器</span>
                    <div className="flex items-center gap-2">
                      <button onClick={() => {
                        if (!problemDetail?.codeSnippets) { setCode(''); return }
                        const slugs = langSlugMap[language] || [language]
                        const matched = problemDetail.codeSnippets.find((s) => slugs.includes(s.langSlug))
                        setCode(matched ? matched.code : problemDetail.codeSnippets[0]?.code || '')
                      }} className="flex items-center gap-1 px-2 py-0.5 text-xs text-gray-400 hover:text-white bg-gray-700 rounded hover:bg-gray-600" title="重置为初始代码模板">
                        <RotateCcw size={12} /> 重置
                      </button>
                    <select value={language} onChange={(e) => {
                      const newLang = e.target.value
                      setLanguage(newLang)
                      // 切换语言时加载对应代码模板
                      if (problemDetail?.codeSnippets) {
                        const slugs = langSlugMap[newLang] || [newLang]
                        const matched = problemDetail.codeSnippets.find((s) => slugs.includes(s.langSlug))
                        if (matched) setCode(matched.code)
                      }
                    }}
                      className="bg-gray-700 border border-gray-600 rounded px-2 py-0.5 text-xs">
                      <option value="java">Java</option>
                      <option value="python3">Python3</option>
                      <option value="cpp">C++</option>
                    </select>
                    </div>
                  </div>
                  <div className="flex-1 overflow-hidden">
                    <Editor
                      height="100%"
                      language={monacoLangMap[language] || language}
                      theme="vs-dark"
                      value={code}
                      onChange={(v) => setCode(v || '')}
                      options={{
                        minimap: { enabled: false },
                        fontSize: 14,
                        scrollBeyondLastLine: false,
                        automaticLayout: true,
                        tabSize: 4,
                        wordWrap: 'on',
                      }}
                    />
                  </div>
                  {/* 底部操作栏 */}
                  <div className="px-4 py-2 border-t border-gray-700 bg-gray-800 flex items-center justify-between shrink-0">
                    <div className="flex items-center gap-3 text-xs text-gray-500">
                      {/* 最近提交状态 */}
                      {selectedProblem && (() => {
                        const subs = mySubmissions.filter((s) => s.problemId === selectedProblem.problemId)
                        if (subs.length === 0) return <span>暂无提交</span>
                        const last = subs[0]
                        return (
                          <span>
                            最近: <span className={last.status === 'Accepted' ? 'text-green-400' : 'text-red-400'}>{last.status}</span>
                            {last.runtime && <span className="ml-1">{last.runtime}</span>}
                            <span className="ml-1 text-gray-600">共{subs.length}次</span>
                          </span>
                        )
                      })()}
                    </div>
                    <button onClick={handleSubmit} disabled={submitting || !code.trim()}
                      className="px-4 py-1.5 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 disabled:opacity-50 flex items-center gap-1.5">
                      {submitting ? <Loader2 size={13} className="animate-spin" /> : <Send size={13} />}
                      {submitting ? '判题中...' : '提交代码'}
                    </button>
                  </div>
                </div>
              </>
            ) : (
              <div className="flex-1 flex items-center justify-center text-gray-500 text-sm">
                <div className="text-center">
                  <div className="text-4xl mb-3 opacity-20">{'</>'}</div>
                  <p>从左侧选择一道题目开始作答</p>
                </div>
              </div>
            )}
          </div>
          )
        )}

        {/* ====== 榜单 Tab ====== */}
        {tab === 'standings' && (
          <div className="h-full overflow-auto p-4">
            {standings ? (
              <div className="bg-gray-800 rounded-lg border border-gray-700 overflow-x-auto">
                {standings.frozen && (
                  <div className="bg-blue-900/20 text-blue-400 text-xs px-4 py-2 flex items-center gap-1">
                    <Lock size={12} /> 当前处于封榜状态，"?" 表示封榜后有新提交
                  </div>
                )}
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-gray-500 border-b border-gray-700">
                      <th className="text-left px-3 py-2 w-12">#</th>
                      <th className="text-left px-3 py-2">{contest.contestType === 'team' ? '队伍' : '用户'}</th>
                      <th className="text-center px-3 py-2">解题</th>
                      <th className="text-center px-3 py-2">{standings.scoringRule === 'acm' ? '罚时' : '总分'}</th>
                      {standings.problems.map((p, i) => (
                        <th key={p.problemId} className="text-center px-2 py-2 w-16">{String.fromCharCode(65 + i)}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {standings.rows.map((row) => (
                      <tr key={row.rank} className="border-b border-gray-700/50 hover:bg-gray-750">
                        <td className="px-3 py-2 font-medium">{row.rank}</td>
                        <td className="px-3 py-2">{row.teamName || row.username}</td>
                        <td className="text-center px-3 py-2">{row.solvedCount}</td>
                        <td className="text-center px-3 py-2">{standings.scoringRule === 'acm' ? formatPenalty(row.totalPenalty) : row.totalScore}</td>
                        {row.problemResults.map((pr) => (
                          <td key={pr.problemId} className="text-center px-2 py-1">
                            {pr.frozen ? (
                              <span className="text-blue-400 text-xs">?{pr.attempts > 0 ? `(${pr.attempts})` : ''}</span>
                            ) : pr.accepted ? (
                              <div>
                                <span className="text-green-400 text-xs">+{pr.attempts > 1 ? pr.attempts - 1 : ''}</span>
                                {pr.firstAcTimeSeconds != null && <div className="text-[10px] text-gray-500">{formatPenalty(pr.firstAcTimeSeconds)}</div>}
                              </div>
                            ) : pr.attempts > 0 ? (
                              <span className="text-red-400 text-xs">-{pr.attempts}</span>
                            ) : null}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="text-center py-20 text-gray-500 text-sm">暂无榜单数据</div>
            )}
          </div>
        )}

        {/* ====== 我的提交 Tab ====== */}
        {tab === 'submissions' && (
          <div className="h-full overflow-auto p-4 space-y-2">
            {mySubmissions.length === 0 ? (
              <p className="text-gray-500 text-sm text-center py-8">暂无提交</p>
            ) : mySubmissions.map((s) => {
              const p = problems.find((pr) => pr.problemId === s.problemId)
              const idx = problems.findIndex((pr) => pr.problemId === s.problemId)
              return (
                <div key={s.id} className="bg-gray-800 rounded border border-gray-700 px-4 py-2.5 flex items-center justify-between text-sm">
                  <div className="flex items-center gap-3">
                    <span className="text-gray-500 text-xs">#{s.id}</span>
                    {idx >= 0 && <span className="text-xs font-bold text-gray-500 bg-gray-700 w-5 h-5 flex items-center justify-center rounded">{String.fromCharCode(65 + idx)}</span>}
                    <span>{p?.title || '未知题目'}</span>
                    <span className="text-gray-600 text-xs">{s.language}</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className={s.status === 'Accepted' ? 'text-green-400' : s.status === 'Pending' ? 'text-gray-400' : 'text-red-400'}>{s.status}</span>
                    {s.runtime && <span className="text-gray-500 text-xs">{s.runtime}</span>}
                    {s.memory && <span className="text-gray-500 text-xs">{s.memory}</span>}
                    {s.score > 0 && <span className="text-yellow-400 text-xs">{s.score}分</span>}
                  </div>
                </div>
              )
            })}
          </div>
        )}

        {/* ====== 队伍 Tab ====== */}
        {tab === 'teams' && (
          <div className="h-full overflow-auto p-4 space-y-4">
            <div className="bg-gray-800 rounded-lg border border-gray-700 p-4">
              <h3 className="text-sm font-medium mb-3">创建/加入队伍</h3>
              <div className="flex gap-4">
                <div className="flex items-center gap-2">
                  <input value={teamName} onChange={(e) => setTeamName(e.target.value)} placeholder="队伍名称"
                    className="bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" />
                  <button onClick={handleCreateTeam} className="px-3 py-1.5 bg-blue-600 text-white rounded text-sm hover:bg-blue-700">创建队伍</button>
                </div>
                <div className="flex items-center gap-2">
                  <input value={inviteCode} onChange={(e) => setInviteCode(e.target.value)} placeholder="邀请码"
                    className="bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" />
                  <button onClick={handleJoinTeam} className="px-3 py-1.5 bg-green-600 text-white rounded text-sm hover:bg-green-700">加入队伍</button>
                </div>
              </div>
            </div>
            <div className="space-y-2">
              {teams.map((t) => (
                <div key={t.id} className="bg-gray-800 rounded border border-gray-700 px-4 py-3 flex items-center justify-between text-sm">
                  <div className="flex items-center gap-3">
                    <Users size={16} className="text-gray-500" />
                    <span className="font-medium">{t.teamName}</span>
                    <span className="text-gray-500">{t.memberCount}人</span>
                  </div>
                  <span className="text-xs text-gray-500">邀请码：{t.inviteCode}</span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
