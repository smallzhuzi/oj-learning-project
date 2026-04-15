import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  ArrowLeft,
  Clock,
  Loader2,
  Lock,
  PanelLeftClose,
  PanelLeftOpen,
  RotateCcw,
  Send,
  Unlock,
  Users,
} from 'lucide-react'
import Editor from '@monaco-editor/react'
import { confirm, toast, useThemeStore } from '@/store/uiStore'
import {
  cancelRegistration,
  contestSubmit,
  getContestDetail,
  getContestProblems,
  getMyContestSubmissions,
  getStandings,
  pollContestResult,
  publishContest,
  registerContest,
  unfreezeStandings,
} from '@/api/contest'
import { getProblemBySlug } from '@/api/problem'
import { getMyCaptainTeams, getIndTeamDetail } from '@/api/team'
import UserMenu from '@/components/UserMenu'
import ThemeToggle from '@/components/ThemeToggle'
import type { ContestDetail, ContestSubmission, IndTeamListItem, IndTeamMember, Problem, ProblemSetItemDetail, StandingData } from '@/types'
import { getProblemTagKey, getProblemTagLabel } from '@/utils/problemTags'

const diffColor: Record<string, string> = {
  Easy: 'text-[var(--success)]',
  EASY: 'text-[var(--success)]',
  Medium: 'text-[var(--warning)]',
  MEDIUM: 'text-[var(--warning)]',
  Hard: 'text-[var(--danger)]',
  HARD: 'text-[var(--danger)]',
}

const diffBgClass: Record<string, string> = {
  Easy: 'diff-easy',
  EASY: 'diff-easy',
  Medium: 'diff-medium',
  MEDIUM: 'diff-medium',
  Hard: 'diff-hard',
  HARD: 'diff-hard',
}

const statusLabelMap: Record<string, string> = {
  draft: '草稿',
  registering: '报名中',
  running: '进行中',
  frozen: '已封榜',
  ended: '已结束',
  archived: '已归档',
}

export default function ContestDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const contestId = Number(id)
  const theme = useThemeStore((s) => s.theme)

  const [contest, setContest] = useState<ContestDetail | null>(null)
  const [problems, setProblems] = useState<ProblemSetItemDetail[]>([])
  const [standings, setStandings] = useState<StandingData | null>(null)
  const [mySubmissions, setMySubmissions] = useState<ContestSubmission[]>([])
  const [selectedProblem, setSelectedProblem] = useState<ProblemSetItemDetail | null>(null)
  const [problemDetail, setProblemDetail] = useState<Problem | null>(null)
  const [code, setCode] = useState('')
  const [language, setLanguage] = useState('java')
  const [submitting, setSubmitting] = useState(false)
  const [tab, setTab] = useState<'problems' | 'standings' | 'submissions'>('problems')
  const [loading, setLoading] = useState(true)
  const [password, setPassword] = useState('')

  const [sidebarOpen, setSidebarOpen] = useState(true)
  const [descWidth, setDescWidth] = useState(45)
  const [dragging, setDragging] = useState(false)
  const containerRef = useRef<HTMLDivElement>(null)
  const [countdown, setCountdown] = useState('')

  // 组队赛队伍选择
  const [teamModalOpen, setTeamModalOpen] = useState(false)
  const [myTeams, setMyTeams] = useState<IndTeamListItem[]>([])
  const [teamsLoading, setTeamsLoading] = useState(false)

  // 出场成员选择（Step 2）
  const [selectedTeamId, setSelectedTeamId] = useState<number | null>(null)
  const [teamMembers, setTeamMembers] = useState<IndTeamMember[]>([])
  const [selectedMemberIds, setSelectedMemberIds] = useState<number[]>([])
  const [membersLoading, setMembersLoading] = useState(false)

  useEffect(() => {
    void loadContest()
  }, [contestId])

  useEffect(() => {
    if (!contest || !['running', 'frozen'].includes(contest.status)) return
    const timer = setInterval(() => {
      const now = new Date().getTime()
      const end = new Date(contest.endTime).getTime()
      const diff = end - now
      if (diff <= 0) {
        setCountdown('00:00:00')
        clearInterval(timer)
        return
      }
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
        const nextContest = res.data
        if (nextContest.status === 'draft' && nextContest.isCreator) {
          navigate(`/contests/${contestId}/edit`, { replace: true })
          return
        }
        setContest(nextContest)
        const canView = ['running', 'frozen', 'ended', 'archived'].includes(nextContest.status) || nextContest.isCreator
        if (canView) {
          void loadProblems()
          void loadStandings()
        }
        if (nextContest.registered) {
          void loadMySubmissions()
        } else {
          setMySubmissions([])
        }
      }
    } catch {}
    setLoading(false)
  }

  const loadProblems = async () => {
    try {
      const res = await getContestProblems(contestId)
      if (res.code === 200) setProblems(res.data)
    } catch {}
  }

  const loadStandings = async () => {
    try {
      const res = await getStandings(contestId)
      if (res.code === 200) setStandings(res.data)
    } catch {}
  }

  const loadMySubmissions = async () => {
    try {
      const res = await getMyContestSubmissions(contestId)
      if (res.code === 200) setMySubmissions(res.data)
    } catch {}
  }

  const handleRegister = async (teamId?: number, memberIds?: number[]) => {
    try {
      await registerContest(contestId, password || undefined, teamId, memberIds)
      setTeamModalOpen(false)
      setSelectedTeamId(null)
      setSelectedMemberIds([])
      await loadContest()
    } catch (e: any) {
      toast.error(e.message)
    }
  }

  const handleOpenTeamModal = async () => {
    setTeamsLoading(true)
    setTeamModalOpen(true)
    setSelectedTeamId(null)
    setSelectedMemberIds([])
    try {
      const res = await getMyCaptainTeams()
      if (res.code === 200) setMyTeams(res.data || [])
    } catch {}
    setTeamsLoading(false)
  }

  /** Step 2：选择队伍后加载成员 */
  const handlePickTeam = async (teamId: number) => {
    setSelectedTeamId(teamId)
    setMembersLoading(true)
    try {
      const res = await getIndTeamDetail(teamId)
      if (res.code === 200) {
        setTeamMembers(res.data.members)
        setSelectedMemberIds(res.data.members.map(m => m.userId))
      }
    } catch {}
    setMembersLoading(false)
  }

  const toggleMember = (userId: number) => {
    setSelectedMemberIds(prev =>
      prev.includes(userId) ? prev.filter(id => id !== userId) : [...prev, userId]
    )
  }

  const handleCancelReg = async () => {
    if (!await confirm('确定取消报名？', { type: 'warning', confirmText: '取消报名' })) return
    try {
      await cancelRegistration(contestId)
      await loadContest()
    } catch (e: any) {
      toast.error(e.message)
    }
  }

  const handlePublish = async () => {
    if (!await confirm('确定发布比赛？', { type: 'warning', confirmText: '发布' })) return
    try {
      await publishContest(contestId)
      await loadContest()
    } catch (e: any) {
      toast.error(e.message)
    }
  }

  const handleUnfreeze = async () => {
    try {
      await unfreezeStandings(contestId)
      await loadContest()
      await loadStandings()
    } catch (e: any) {
      toast.error(e.message)
    }
  }

  const monacoLangMap: Record<string, string> = { java: 'java', python3: 'python', cpp: 'cpp', c: 'c', javascript: 'javascript', typescript: 'typescript', go: 'go', rust: 'rust' }
  const langSlugMap: Record<string, string[]> = { java: ['java'], python3: ['python3', 'python'], cpp: ['cpp', 'c++'] }

  const handleSelectProblem = async (item: ProblemSetItemDetail) => {
    setSelectedProblem(item)
    try {
      const res = await getProblemBySlug(item.slug, contest?.ojPlatform || 'leetcode')
      if (res.code === 200) {
        setProblemDetail(res.data)
        const snippets = res.data.codeSnippets
        if (snippets && snippets.length > 0) {
          const slugs = langSlugMap[language] || [language]
          const matched = snippets.find((snippet) => slugs.includes(snippet.langSlug))
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
            const result = await pollContestResult(contestId, subId)
            if (result.code === 200 && result.data.status !== 'Pending') {
              clearInterval(poll)
              setSubmitting(false)
              void loadMySubmissions()
              void loadStandings()
            }
          } catch {
            clearInterval(poll)
            setSubmitting(false)
          }
        }, 2000)
      }
    } catch (e: any) {
      toast.error(e.message)
      setSubmitting(false)
    }
  }

  const formatPenalty = (seconds: number) => {
    const h = Math.floor(seconds / 3600)
    const m = Math.floor((seconds % 3600) / 60)
    return `${h}:${m.toString().padStart(2, '0')}`
  }

  const handleDragStart = () => setDragging(true)
  useEffect(() => {
    if (!dragging) return
    const handleMove = (e: MouseEvent) => {
      if (!containerRef.current) return
      const rect = containerRef.current.getBoundingClientRect()
      const sidebarW = sidebarOpen ? 220 : 0
      const usable = rect.width - sidebarW
      const x = e.clientX - rect.left - sidebarW
      setDescWidth(Math.min(70, Math.max(25, (x / usable) * 100)))
    }
    const handleUp = () => setDragging(false)
    window.addEventListener('mousemove', handleMove)
    window.addEventListener('mouseup', handleUp)
    return () => {
      window.removeEventListener('mousemove', handleMove)
      window.removeEventListener('mouseup', handleUp)
    }
  }, [dragging, sidebarOpen])

  const editorTheme = theme === 'dark' ? 'vs-dark' : 'light'

  if (loading) return <div className="min-h-screen theme-bg flex items-center justify-center theme-faint"><Loader2 className="animate-spin mr-2" /> 加载中...</div>
  if (!contest) return <div className="min-h-screen theme-bg flex items-center justify-center theme-faint">比赛不存在</div>

  const isActive = ['running', 'frozen'].includes(contest.status)
  const contestTypeLabel = contest.contestType === 'team' ? '组队赛' : '个人赛'
  const scoringLabel = contest.scoringRule === 'acm' ? 'ACM 罚时制' : contest.scoringRule === 'oi' ? 'OI 分数制' : 'CF 风格'
  const registerCountLabel = `${contest.registeredCount} ${contest.contestType === 'team' ? '队' : '人'}`

  return (
    <div className="h-screen flex flex-col theme-bg overflow-hidden">
      <header className="theme-header px-4 py-2 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/contests')} className="theme-button-ghost"><ArrowLeft size={16} /></button>
          <h1 className="font-semibold text-sm theme-text">{contest.title}</h1>
          <span className={`text-xs px-2 py-0.5 rounded-full theme-tag ${
            isActive ? 'text-[var(--success)]' : contest.status === 'frozen' ? 'text-[var(--info)]' : ''
          }`}>
            {statusLabelMap[contest.status] || contest.status}
          </span>
          {isActive && countdown && (
            <span className="text-sm font-mono flex items-center gap-1" style={{ color: 'var(--success)' }}><Clock size={13} />{countdown}</span>
          )}
        </div>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-3 text-xs theme-faint">
            <span>{contestTypeLabel}</span>
            <span>{contest.scoringRule === 'acm' ? 'ACM' : contest.scoringRule === 'oi' ? 'OI' : 'CF'}</span>
            <span>{registerCountLabel}</span>
          </div>
          {contest.isCreator && contest.status === 'draft' && (
            <button onClick={handlePublish} className="px-2 py-1 theme-button-blue rounded-lg text-xs">发布</button>
          )}
          {contest.isCreator && (contest.status === 'frozen' || contest.status === 'ended') && (
            <button onClick={handleUnfreeze} className="px-2 py-1 rounded-lg text-xs bg-gradient-to-r from-amber-500 to-amber-600 text-white flex items-center gap-1"><Unlock size={12} />解封</button>
          )}
          {contest.contestType === 'team' && !contest.registered && (contest.status === 'registering' || contest.status === 'running') && (
            <button onClick={handleOpenTeamModal} className="px-2 py-1 theme-button-success rounded-lg text-xs">选择队伍报名</button>
          )}
          {contest.contestType !== 'team' && !contest.registered && (contest.status === 'registering' || contest.status === 'running') && (
            <button onClick={() => void handleRegister()} className="px-2 py-1 theme-button-success rounded-lg text-xs">报名</button>
          )}
          {contest.contestType !== 'team' && contest.registered && contest.status === 'registering' && (
            <button onClick={handleCancelReg} className="px-2 py-1 theme-button-secondary rounded-lg text-xs">取消报名</button>
          )}
          <ThemeToggle />
          <UserMenu />
        </div>
      </header>

      <div className="flex gap-0.5 px-4 theme-header shrink-0">
        {[
          { key: 'problems' as const, label: '题目' },
          { key: 'standings' as const, label: '榜单' },
          { key: 'submissions' as const, label: '我的提交' },
        ].map((item) => (
          <button key={item.key} onClick={() => setTab(item.key)}
            className={`px-4 py-2 text-xs font-medium border-b-2 transition-colors ${
              tab === item.key ? 'border-[var(--accent)] theme-accent-text' : 'border-transparent theme-faint hover:text-[var(--text-primary)]'
            }`}>{item.label}</button>
        ))}
      </div>

      <div className="flex-1 overflow-hidden">
        {tab === 'problems' && (
          ['draft', 'registering'].includes(contest.status) && problems.length === 0 ? (
            <div className="h-full flex items-center justify-center">
              <div className="max-w-md text-center space-y-4">
                {contest.isCreator ? (
                  <>
                    <h2 className="text-lg font-medium theme-text">{contest.status === 'draft' ? '比赛草稿' : '报名进行中'}</h2>
                    <div className="theme-card rounded-2xl p-4 text-left text-sm space-y-2">
                      {[
                        ['比赛类型', contest.contestType === 'team' ? '组队赛' : '个人赛'],
                        ['计分规则', contest.scoringRule === 'acm' ? 'ACM 罚时制' : contest.scoringRule === 'oi' ? 'OI 分数制' : 'CF 风格'],
                        ['开始时间', new Date(contest.startTime).toLocaleString('zh-CN')],
                        ['时长', `${contest.durationMinutes} 分钟`],
                        ['题目', contest.problemCount === 0 ? '未设置题目' : `${contest.problemCount} 题`],
                        [contest.contestType === 'team' ? '已报名队伍' : '报名人数', `${contest.registeredCount} ${contest.contestType === 'team' ? '队' : '人'}`],
                      ].map(([label, value]) => (
                        <div key={label} className="flex justify-between">
                          <span className="theme-faint">{label}</span>
                          <span className={label === '题目' && contest.problemCount === 0 ? 'text-[var(--danger)]' : label === '题目' ? 'text-[var(--success)]' : 'theme-text'}>{value}</span>
                        </div>
                      ))}
                    </div>
                    {contest.status === 'draft' && (
                      <div className="space-y-2">
                        {contest.problemCount === 0 && <p className="text-xs text-[var(--warning)]">请先在创建比赛页面设置题目，才能发布比赛。</p>}
                        <button onClick={handlePublish} disabled={contest.problemCount === 0}
                          className="px-5 py-2 theme-button-blue rounded-xl text-sm font-medium disabled:opacity-40 disabled:cursor-not-allowed">发布比赛</button>
                      </div>
                    )}
                  </>
                ) : (
                  <>
                    <h2 className="text-lg font-medium theme-text">{contest.status === 'draft' ? '比赛尚未发布' : '比赛未开始'}</h2>
                    <p className="text-sm theme-faint">{contest.status === 'draft' ? '该比赛正在筹备中，请等待创建者发布。' : `比赛将于 ${new Date(contest.startTime).toLocaleString('zh-CN')} 开始。`}</p>
                    {contest.status === 'registering' && contest.contestType !== 'team' && !contest.registered && (
                      <div className="flex items-center justify-center gap-2">
                        {!contest.isPublic && <input value={password} onChange={(e) => setPassword(e.target.value)} placeholder="比赛密码" type="password" className="theme-input rounded-lg px-3 py-1.5 text-sm w-32" />}
                        <button onClick={() => void handleRegister()} className="px-4 py-1.5 theme-button-success rounded-lg text-sm">立即报名</button>
                      </div>
                    )}
                    {contest.status === 'registering' && contest.contestType === 'team' && !contest.registered && (
                      <div className="space-y-2">
                        <p className="text-sm theme-faint">组队赛需要选择一个你作为队长的队伍来报名。</p>
                        <button onClick={handleOpenTeamModal} className="px-4 py-1.5 theme-button-success rounded-lg text-sm">选择队伍报名</button>
                      </div>
                    )}
                    {contest.registered && <p className="text-xs text-[var(--success)]">{contest.contestType === 'team' ? '你的队伍已完成报名，等待比赛开始。' : '你已报名，等待比赛开始。'}</p>}
                  </>
                )}
              </div>
            </div>
          ) : (
            <div ref={containerRef} className="flex h-full" style={{ userSelect: dragging ? 'none' : 'auto' }}>
              <div className={`shrink-0 theme-sidebar transition-all duration-200 flex flex-col ${sidebarOpen ? 'w-[220px]' : 'w-0'}`} style={{ overflow: 'hidden' }}>
                <div className="px-3 py-2 border-b theme-border flex items-center justify-between">
                  <span className="text-xs font-medium theme-faint">题目列表</span>
                  <button onClick={() => setSidebarOpen(false)} className="theme-hint hover:text-[var(--text-primary)]"><PanelLeftClose size={14} /></button>
                </div>
                <div className="flex-1 overflow-y-auto py-1">
                  {problems.length === 0 ? (
                    <p className="theme-faint text-xs py-6 text-center">暂无题目</p>
                  ) : problems.map((problem, index) => {
                    const mySubs = mySubmissions.filter((submission) => submission.problemId === problem.problemId)
                    const isAc = mySubs.some((submission) => submission.status === 'Accepted')
                    const attempts = mySubs.length
                    const isSelected = selectedProblem?.id === problem.id
                    return (
                      <div key={problem.id} onClick={() => void handleSelectProblem(problem)}
                        className={`flex items-center gap-2 px-3 py-2 cursor-pointer text-xs transition-colors ${
                          isSelected ? 'bg-[var(--accent-soft)] border-l-2 border-[var(--accent)]' : 'theme-hover border-l-2 border-transparent'
                        }`}>
                        <span className={`w-5 h-5 flex items-center justify-center rounded text-xs font-bold shrink-0 ${
                          isAc ? 'theme-status-accepted' : attempts > 0 ? 'theme-status-error' : 'theme-tag'
                        }`}>{String.fromCharCode(65 + index)}</span>
                        <div className="flex-1 min-w-0">
                          <div className="truncate theme-text">{problem.title}</div>
                          <div className="flex items-center gap-1.5 mt-0.5">
                            <span className={`text-[10px] ${diffColor[problem.difficulty] || 'theme-faint'}`}>{problem.difficulty}</span>
                            {isAc && <span className="text-[10px] text-[var(--success)]">AC</span>}
                            {attempts > 0 && !isAc && <span className="text-[10px] text-[var(--danger)]">{attempts} 次</span>}
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              </div>

              {!sidebarOpen && (
                <button onClick={() => setSidebarOpen(true)}
                  className="shrink-0 w-6 theme-sidebar flex items-center justify-center theme-hover theme-hint hover:text-[var(--text-primary)]">
                  <PanelLeftOpen size={14} />
                </button>
              )}

              {selectedProblem && problemDetail ? (
                <>
                  <div className="flex flex-col overflow-hidden" style={{ width: `${descWidth}%` }}>
                    <div className="px-4 py-2 border-b theme-border theme-header flex items-center gap-2 shrink-0">
                      <span className="text-xs font-bold theme-faint">{String.fromCharCode(65 + problems.findIndex((item) => item.id === selectedProblem.id))}</span>
                      <span className="text-sm font-medium theme-text">{problemDetail.frontendId}. {problemDetail.title}</span>
                      <span className={`text-xs px-1.5 py-0.5 rounded ${diffBgClass[problemDetail.difficulty] || 'theme-tag'}`}>{problemDetail.difficulty}</span>
                    </div>
                    <div className="flex-1 overflow-y-auto p-4">
                      {problemDetail.topicTags && problemDetail.topicTags.length > 0 && (
                        <div className="mb-4 flex flex-wrap gap-2">
                          {problemDetail.topicTags.map((tag, index) => (
                            <span key={getProblemTagKey(tag, index)} className="rounded-full px-2 py-1 text-[11px] theme-chip">{getProblemTagLabel(tag)}</span>
                          ))}
                        </div>
                      )}
                      <div className="theme-prose max-w-none text-sm" dangerouslySetInnerHTML={{ __html: problemDetail.contentMarkdown || '' }} />
                    </div>
                  </div>

                  <div onMouseDown={handleDragStart}
                    className={`w-1 shrink-0 cursor-col-resize transition-colors ${dragging ? 'bg-[var(--accent)]' : 'theme-drag-h'}`} />

                  <div className="flex-1 flex flex-col overflow-hidden min-w-0">
                    <div className="px-4 py-2 border-b theme-border theme-header flex items-center justify-between shrink-0">
                      <span className="text-xs theme-faint">代码编辑器</span>
                      <div className="flex items-center gap-2">
                        <button onClick={() => {
                          if (!problemDetail?.codeSnippets) { setCode(''); return }
                          const slugs = langSlugMap[language] || [language]
                          const matched = problemDetail.codeSnippets.find((snippet) => slugs.includes(snippet.langSlug))
                          setCode(matched ? matched.code : problemDetail.codeSnippets[0]?.code || '')
                        }} className="flex items-center gap-1 px-2 py-0.5 text-xs theme-button-ghost rounded" title="重置为初始代码模板">
                          <RotateCcw size={12} /> 重置
                        </button>
                        <select value={language} onChange={(e) => {
                          const newLang = e.target.value
                          setLanguage(newLang)
                          if (problemDetail?.codeSnippets) {
                            const slugs = langSlugMap[newLang] || [newLang]
                            const matched = problemDetail.codeSnippets.find((snippet) => slugs.includes(snippet.langSlug))
                            if (matched) setCode(matched.code)
                          }
                        }} className="theme-select rounded px-2 py-0.5 text-xs">
                          <option value="java">Java</option>
                          <option value="python3">Python3</option>
                          <option value="cpp">C++</option>
                        </select>
                      </div>
                    </div>
                    <div className="flex-1 overflow-hidden">
                      <Editor height="100%" language={monacoLangMap[language] || language} theme={editorTheme} value={code} onChange={(value) => setCode(value || '')}
                        options={{ minimap: { enabled: false }, fontSize: 14, scrollBeyondLastLine: false, automaticLayout: true, tabSize: 4, wordWrap: 'on' }} />
                    </div>
                    <div className="px-4 py-2 border-t theme-border theme-header flex items-center justify-between shrink-0">
                      <div className="flex items-center gap-3 text-xs theme-faint">
                        {selectedProblem && (() => {
                          const subs = mySubmissions.filter((submission) => submission.problemId === selectedProblem.problemId)
                          if (subs.length === 0) return <span>暂无提交</span>
                          const last = subs[0]
                          return (
                            <span>
                              最近：<span className={last.status === 'Accepted' ? 'text-[var(--success)]' : 'text-[var(--danger)]'}>{last.status}</span>
                              {last.runtime && <span className="ml-1">{last.runtime}</span>}
                              <span className="ml-1 theme-hint">共 {subs.length} 次</span>
                            </span>
                          )
                        })()}
                      </div>
                      <button onClick={handleSubmit} disabled={submitting || !code.trim()}
                        className="px-4 py-1.5 theme-button-blue rounded-lg text-sm disabled:opacity-50 flex items-center gap-1.5">
                        {submitting ? <Loader2 size={13} className="animate-spin" /> : <Send size={13} />}
                        {submitting ? '判题中...' : '提交代码'}
                      </button>
                    </div>
                  </div>
                </>
              ) : (
                <div className="flex-1 flex items-center justify-center theme-faint text-sm">
                  <div className="text-center">
                    <div className="text-4xl mb-3 opacity-20">{'</>'}</div>
                    <p>从左侧选择一道题目开始作答</p>
                  </div>
                </div>
              )}
            </div>
          )
        )}

        {tab === 'standings' && (
          <div className="h-full overflow-auto p-4">
            {standings ? (
              <div className="theme-card rounded-2xl overflow-x-auto">
                {standings.frozen && (
                  <div className="theme-status-info text-xs px-4 py-2 flex items-center gap-1">
                    <Lock size={12} /> 当前处于封榜状态，`?` 表示封榜后仍有提交变化
                  </div>
                )}
                <table className="w-full text-sm">
                  <thead>
                    <tr className="theme-faint border-b theme-border">
                      <th className="text-left px-3 py-2 w-12">#</th>
                      <th className="text-left px-3 py-2">{contest.contestType === 'team' ? '队伍' : '用户'}</th>
                      <th className="text-center px-3 py-2">解题</th>
                      <th className="text-center px-3 py-2">{standings.scoringRule === 'acm' ? '罚时' : '总分'}</th>
                      {standings.problems.map((problem, index) => (
                        <th key={problem.problemId} className="text-center px-2 py-2 w-16">{String.fromCharCode(65 + index)}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {standings.rows.map((row) => (
                      <tr key={row.rank} className="border-b theme-border theme-hover">
                        <td className="px-3 py-2 font-medium theme-text">{row.rank}</td>
                        <td className="px-3 py-2 theme-text">{row.teamName || row.username}</td>
                        <td className="text-center px-3 py-2 theme-text">{row.solvedCount}</td>
                        <td className="text-center px-3 py-2 theme-text">{standings.scoringRule === 'acm' ? formatPenalty(row.totalPenalty) : row.totalScore}</td>
                        {row.problemResults.map((result) => (
                          <td key={result.problemId} className="text-center px-2 py-1">
                            {result.frozen ? (
                              <span className="text-[var(--info)] text-xs">?{result.attempts > 0 ? `(${result.attempts})` : ''}</span>
                            ) : result.accepted ? (
                              <div>
                                <span className="text-[var(--success)] text-xs">+{result.attempts > 1 ? result.attempts - 1 : ''}</span>
                                {result.firstAcTimeSeconds != null && <div className="text-[10px] theme-hint">{formatPenalty(result.firstAcTimeSeconds)}</div>}
                              </div>
                            ) : result.attempts > 0 ? (
                              <span className="text-[var(--danger)] text-xs">-{result.attempts}</span>
                            ) : null}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="text-center py-20 theme-faint text-sm">暂无榜单数据</div>
            )}
          </div>
        )}

        {tab === 'submissions' && (
          <div className="h-full overflow-auto p-4 space-y-2">
            {mySubmissions.length === 0 ? (
              <p className="theme-faint text-sm text-center py-8">暂无提交</p>
            ) : mySubmissions.map((submission) => {
              const problem = problems.find((item) => item.problemId === submission.problemId)
              const index = problems.findIndex((item) => item.problemId === submission.problemId)
              return (
                <div key={submission.id} className="theme-card rounded-xl px-4 py-2.5 flex items-center justify-between text-sm">
                  <div className="flex items-center gap-3">
                    <span className="theme-hint text-xs">#{submission.id}</span>
                    {index >= 0 && <span className="text-xs font-bold theme-tag w-5 h-5 flex items-center justify-center rounded">{String.fromCharCode(65 + index)}</span>}
                    <span className="theme-text">{problem?.title || '未知题目'}</span>
                    <span className="theme-hint text-xs">{submission.language}</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className={submission.status === 'Accepted' ? 'text-[var(--success)]' : submission.status === 'Pending' ? 'theme-faint' : 'text-[var(--danger)]'}>{submission.status}</span>
                    {submission.runtime && <span className="theme-faint text-xs">{submission.runtime}</span>}
                    {submission.memory && <span className="theme-faint text-xs">{submission.memory}</span>}
                    {submission.score > 0 && <span className="text-[var(--warning)] text-xs">{submission.score} 分</span>}
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>

      {/* 组队赛队伍选择弹窗（两步流程） */}
      {teamModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center theme-overlay" onClick={() => { setTeamModalOpen(false); setSelectedTeamId(null) }}>
          <div className="theme-modal rounded-2xl w-[480px] max-h-[70vh] flex flex-col" onClick={(e) => e.stopPropagation()}>
            {/* 弹窗头部 */}
            <div className="flex items-center justify-between px-5 py-3 border-b theme-border">
              <div className="flex items-center gap-2">
                {selectedTeamId && (
                  <button onClick={() => { setSelectedTeamId(null); setSelectedMemberIds([]) }} className="theme-faint hover:text-[var(--text-primary)] text-sm">&larr;</button>
                )}
                <h3 className="font-medium theme-text">{selectedTeamId ? '选择出场成员' : '选择队伍报名'}</h3>
              </div>
              <button onClick={() => { setTeamModalOpen(false); setSelectedTeamId(null) }} className="theme-faint hover:text-[var(--text-primary)] text-lg">&times;</button>
            </div>

            {/* 步骤指示器 */}
            <div className="flex items-center gap-2 px-5 py-2 border-b theme-border">
              <span className={`text-xs px-2 py-0.5 rounded-full ${!selectedTeamId ? 'bg-[var(--accent)] text-white' : 'theme-tag'}`}>1. 选队伍</span>
              <span className="theme-hint text-xs">&rarr;</span>
              <span className={`text-xs px-2 py-0.5 rounded-full ${selectedTeamId ? 'bg-[var(--accent)] text-white' : 'theme-tag'}`}>2. 选成员</span>
            </div>

            <div className="flex-1 overflow-y-auto p-4 space-y-2">
              {!selectedTeamId ? (
                /* Step 1：选择队伍 */
                teamsLoading ? (
                  <div className="flex items-center justify-center py-8 theme-faint"><Loader2 className="animate-spin mr-2" size={16} />加载中...</div>
                ) : myTeams.length === 0 ? (
                  <div className="text-center py-8 space-y-2">
                    <p className="theme-faint text-sm">你还没有作为队长的队伍</p>
                    <button onClick={() => { setTeamModalOpen(false); navigate('/teams') }} className="theme-button-blue rounded-lg px-4 py-2 text-sm">前往队伍空间创建</button>
                  </div>
                ) : myTeams.map((team) => {
                  const minSize = contest?.minTeamSize || 1
                  const maxSize = contest?.maxTeamSize || 99
                  const eligible = team.memberCount >= minSize && team.memberCount <= maxSize
                  return (
                    <div key={team.id} className="theme-surface rounded-xl p-3 flex items-center justify-between">
                      <div>
                        <div className="flex items-center gap-2">
                          <Users size={14} className="theme-faint" />
                          <span className="font-medium theme-text">{team.teamName}</span>
                          <span className="text-[11px] px-2 py-0.5 rounded-full theme-tag">{team.memberCount} 人</span>
                        </div>
                        {!eligible && <div className="text-xs text-[var(--danger)] mt-0.5">人数需 {minSize}~{maxSize} 人</div>}
                      </div>
                      <button onClick={() => void handlePickTeam(team.id)} disabled={!eligible}
                        className="theme-button-blue rounded-lg px-3 py-1.5 text-xs disabled:opacity-40">
                        选择
                      </button>
                    </div>
                  )
                })
              ) : (
                /* Step 2：勾选出场成员 */
                membersLoading ? (
                  <div className="flex items-center justify-center py-8 theme-faint"><Loader2 className="animate-spin mr-2" size={16} />加载成员...</div>
                ) : (
                  <>
                    <p className="text-xs theme-faint">勾选本次比赛的出场成员（至少 {contest?.minTeamSize || 1} 人，至多 {contest?.maxTeamSize || 99} 人）：</p>
                    {teamMembers.map((member) => (
                      <label key={member.userId} className="theme-surface rounded-xl p-3 flex items-center gap-3 cursor-pointer hover:brightness-95 transition-all">
                        <input type="checkbox" checked={selectedMemberIds.includes(member.userId)} onChange={() => toggleMember(member.userId)}
                          className="w-4 h-4 rounded accent-[var(--accent)]" />
                        <div className="flex-1">
                          <span className="font-medium theme-text text-sm">{member.username}</span>
                          {member.role === 'captain' && <span className="ml-2 text-[10px] px-1.5 py-0.5 rounded-full bg-[var(--accent-soft)] theme-accent-text">队长</span>}
                        </div>
                      </label>
                    ))}
                  </>
                )
              )}
            </div>

            {/* Step 2 底部确认按钮 */}
            {selectedTeamId && !membersLoading && (
              <div className="px-5 py-3 border-t theme-border flex items-center justify-between">
                <span className="text-xs theme-faint">已选 {selectedMemberIds.length} 人</span>
                <button
                  onClick={() => void handleRegister(selectedTeamId, selectedMemberIds)}
                  disabled={selectedMemberIds.length < (contest?.minTeamSize || 1) || selectedMemberIds.length > (contest?.maxTeamSize || 99)}
                  className="theme-button-blue rounded-lg px-4 py-1.5 text-sm disabled:opacity-40 disabled:cursor-not-allowed">
                  确认报名
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
