import { useState, useEffect, useCallback } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Loader2, Search, X, Plus, Trash2, GripVertical, Zap, Bot } from 'lucide-react'
import { createContest, updateContest, publishContest, getContestDetail, getContestProblems } from '@/api/contest'
import { getProblems } from '@/api/problem'
import { quickGenerate, getMyProblemSets, getProblemSetItems } from '@/api/problemSet'
import { smartGenerateStream } from '@/api/dify'
import { toast, confirm } from '@/store/uiStore'
import UserMenu from '@/components/UserMenu'
import type { CreateContestParams, Problem, ProblemSet, ProblemSetItemDetail } from '@/types'

/** 难度颜色 */
const diffColor: Record<string, string> = {
  Easy: 'text-green-400 bg-green-900/30',
  EASY: 'text-green-400 bg-green-900/30',
  Medium: 'text-yellow-400 bg-yellow-900/30',
  MEDIUM: 'text-yellow-400 bg-yellow-900/30',
  Hard: 'text-red-400 bg-red-900/30',
  HARD: 'text-red-400 bg-red-900/30',
}

/** 已选题目项 */
interface SelectedProblem {
  slug: string
  title: string
  frontendId: string
  difficulty: string
  score: number
}

export default function CreateContestPage() {
  const navigate = useNavigate()
  const { id: editId } = useParams<{ id: string }>()
  const isEditMode = !!editId
  const [submitting, setSubmitting] = useState(false)
  const [pageLoading, setPageLoading] = useState(false)

  // 基本信息
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [contestType, setContestType] = useState<'individual' | 'team'>('individual')
  const [startDate, setStartDate] = useState('')
  const [startTime, setStartTime] = useState('14:00')
  const [duration, setDuration] = useState(120)
  const [freezeMinutes, setFreezeMinutes] = useState(0)
  const [scoringRule, setScoringRule] = useState('acm')
  const [penaltyTime, setPenaltyTime] = useState(20)
  const [maxParticipants, setMaxParticipants] = useState(0)
  const [maxTeamSize, setMaxTeamSize] = useState(3)
  const [isPublic, setIsPublic] = useState(true)
  const [password, setPassword] = useState('')

  // 已选题目列表
  const [selectedProblems, setSelectedProblems] = useState<SelectedProblem[]>([])

  // 模态框状态
  const [modalOpen, setModalOpen] = useState(false)
  const [modalTab, setModalTab] = useState<'search' | 'quick' | 'smart' | 'problemset'>('search')

  // 搜索选题
  const [searchKeyword, setSearchKeyword] = useState('')
  const [searchDifficulty, setSearchDifficulty] = useState('')
  const [searchResults, setSearchResults] = useState<Problem[]>([])
  const [searchLoading, setSearchLoading] = useState(false)
  const [searchTotal, setSearchTotal] = useState(0)
  const [searchPage, setSearchPage] = useState(1)

  // 快速组题
  const [quickCount, setQuickCount] = useState(5)
  const [quickDiff, setQuickDiff] = useState('intermediate')
  const [quickLoading, setQuickLoading] = useState(false)

  // 智能组题
  const [smartCount, setSmartCount] = useState(5)
  const [smartSelf, setSmartSelf] = useState('')
  const [smartGoal, setSmartGoal] = useState('')
  const [smartPref, setSmartPref] = useState('')
  const [smartLoading, setSmartLoading] = useState(false)
  const [smartResponse, setSmartResponse] = useState('')

  // 已有题单
  const [problemSets, setProblemSets] = useState<ProblemSet[]>([])
  const [psLoading, setPsLoading] = useState(false)

  // 编辑模式：加载已有比赛数据
  useEffect(() => {
    if (!isEditMode) return
    setPageLoading(true)
    ;(async () => {
      try {
        const res = await getContestDetail(Number(editId))
        if (res.code === 200) {
          const c = res.data
          setTitle(c.title)
          setDescription(c.description || '')
          setContestType(c.contestType)
          const st = new Date(c.startTime)
          setStartDate(st.toISOString().slice(0, 10))
          setStartTime(st.toTimeString().slice(0, 5))
          setDuration(c.durationMinutes)
          setFreezeMinutes(c.freezeMinutes)
          setScoringRule(c.scoringRule)
          setPenaltyTime(c.penaltyTime)
          setMaxParticipants(c.maxParticipants)
          setMaxTeamSize(c.maxTeamSize)
          setIsPublic(c.isPublic)

          // 加载已有题目
          if (c.problemCount > 0) {
            try {
              const itemsRes = await getContestProblems(Number(editId))
              if (itemsRes.code === 200) {
                setSelectedProblems(itemsRes.data.map((item: ProblemSetItemDetail) => ({
                  slug: item.slug,
                  title: item.title,
                  frontendId: item.frontendId,
                  difficulty: item.difficulty,
                  score: item.score,
                })))
              }
            } catch {}
          }
        }
      } catch {}
      setPageLoading(false)
    })()
  }, [editId])

  // 搜索题目
  const handleSearch = async (page = 1) => {
    setSearchLoading(true)
    setSearchPage(page)
    try {
      const res = await getProblems({
        keyword: searchKeyword || undefined,
        difficulty: searchDifficulty || undefined,
        pageNum: page,
        pageSize: 10,
      })
      if (res.code === 200) {
        setSearchResults(res.data.records)
        setSearchTotal(res.data.total)
      }
    } catch {}
    setSearchLoading(false)
  }

  // 打开模态框时加载题单列表
  const openModal = () => {
    setModalOpen(true)
    handleSearch(1)
    loadProblemSets()
  }

  const loadProblemSets = async () => {
    setPsLoading(true)
    try {
      const res = await getMyProblemSets(1, 50)
      if (res.code === 200) setProblemSets(res.data.records)
    } catch {}
    setPsLoading(false)
  }

  // 添加题目
  const addProblem = (p: Problem) => {
    if (selectedProblems.some((sp) => sp.slug === p.slug)) return
    setSelectedProblems((prev) => [
      ...prev,
      {
        slug: p.slug,
        title: p.title,
        frontendId: p.frontendId || '',
        difficulty: p.difficulty,
        score: 100,
      },
    ])
  }

  // 移除题目
  const removeProblem = (slug: string) => {
    setSelectedProblems((prev) => prev.filter((p) => p.slug !== slug))
  }

  // 修改分值
  const updateScore = (slug: string, score: number) => {
    setSelectedProblems((prev) =>
      prev.map((p) => (p.slug === slug ? { ...p, score } : p)),
    )
  }

  // 移动题目顺序
  const moveProblem = (index: number, direction: -1 | 1) => {
    const newIndex = index + direction
    if (newIndex < 0 || newIndex >= selectedProblems.length) return
    const newList = [...selectedProblems]
    ;[newList[index], newList[newIndex]] = [newList[newIndex], newList[index]]
    setSelectedProblems(newList)
  }

  // 快速组题
  const handleQuickGenerate = async () => {
    setQuickLoading(true)
    try {
      const res = await quickGenerate({ count: quickCount, difficultyLevel: quickDiff, excludeSolved: false })
      if (res.code === 200 && res.data.id) {
        const itemsRes = await getProblemSetItems(res.data.id)
        if (itemsRes.code === 200) {
          const newProblems: SelectedProblem[] = itemsRes.data.map((item: ProblemSetItemDetail) => ({
            slug: item.slug,
            title: item.title,
            frontendId: item.frontendId,
            difficulty: item.difficulty,
            score: item.score,
          }))
          setSelectedProblems(newProblems)
          setModalOpen(false)
        }
      }
    } catch (e: any) {
      toast.error(e.message || '组题失败')
    }
    setQuickLoading(false)
  }

  // 智能组题
  const handleSmartGenerate = () => {
    if (!smartSelf.trim() || !smartGoal.trim()) { toast.warning('请填写水平和目标'); return }
    setSmartLoading(true)
    setSmartResponse('')

    smartGenerateStream(
      { count: smartCount, selfAssessment: smartSelf, targetGoal: smartGoal, preference: smartPref || undefined },
      {
        onChunk: (chunk) => setSmartResponse((prev) => prev + chunk),
        onDone: (resp) => {
          setSmartLoading(false)
          // 解析 slug 列表
          const match = resp.answer?.match(/\[PROBLEM_SET\]([\s\S]*?)\[\/PROBLEM_SET\]/)
          if (match) {
            const lines = match[1].trim().split('\n')
            const newProblems: SelectedProblem[] = []
            for (const line of lines) {
              const parts = line.match(/^\d+\.\s*([a-z0-9-]+)\s*\|\s*(\w+)\s*\|/)
              if (parts) {
                newProblems.push({
                  slug: parts[1],
                  title: parts[1],
                  frontendId: '',
                  difficulty: parts[2],
                  score: 100,
                })
              }
            }
            if (newProblems.length > 0) {
              setSelectedProblems(newProblems)
              setModalOpen(false)
            }
          }
        },
        onError: (err) => { setSmartLoading(false); toast.error('组题失败：' + err) },
      },
    ).catch((e) => { setSmartLoading(false); toast.error(e.message) })
  }

  // 导入题单
  const handleImportProblemSet = async (psId: number) => {
    try {
      const res = await getProblemSetItems(psId)
      if (res.code === 200) {
        const newProblems: SelectedProblem[] = res.data.map((item: ProblemSetItemDetail) => ({
          slug: item.slug,
          title: item.title,
          frontendId: item.frontendId,
          difficulty: item.difficulty,
          score: item.score,
        }))
        setSelectedProblems(newProblems)
        setModalOpen(false)
      }
    } catch {}
  }

  // 构建参数
  const buildParams = (): CreateContestParams | null => {
    if (!title.trim()) { toast.warning('请输入比赛标题'); return null }
    if (!startDate) { toast.warning('请选择开始日期'); return null }
    if (selectedProblems.length === 0) { toast.warning('请至少选择一道题目'); return null }
    return {
      title,
      description: description || undefined,
      contestType,
      startTime: `${startDate}T${startTime}:00`,
      durationMinutes: duration,
      freezeMinutes,
      scoringRule,
      penaltyTime,
      maxParticipants,
      maxTeamSize,
      isPublic,
      password: password || undefined,
      problemSource: 'manual',
      problems: selectedProblems.map((p) => ({ slug: p.slug, score: p.score })),
    }
  }

  // 保存草稿 / 创建
  const handleSubmit = async () => {
    const params = buildParams()
    if (!params) return
    setSubmitting(true)
    try {
      if (isEditMode) {
        await updateContest(Number(editId), params)
        navigate(`/contests/${editId}`)
      } else {
        const res = await createContest(params)
        if (res.code === 200) navigate(`/contests/${res.data.id}`)
      }
    } catch (e: any) {
      toast.error(e.message || (isEditMode ? '保存失败' : '创建失败'))
    }
    setSubmitting(false)
  }

  // 保存并发布
  const [publishing, setPublishing] = useState(false)
  const handleSaveAndPublish = async () => {
    const params = buildParams()
    if (!params) return
    if (!await confirm('确定保存并发布比赛？发布后将进入报名状态。', { type: 'warning', confirmText: '保存并发布' })) return
    setPublishing(true)
    try {
      await updateContest(Number(editId), params)
      await publishContest(Number(editId))
      navigate(`/contests/${editId}`)
    } catch (e: any) {
      toast.error(e.message || '发布失败')
    }
    setPublishing(false)
  }

  const searchTotalPages = Math.ceil(searchTotal / 10)

  return (
    <div className="min-h-screen bg-gray-900 text-gray-100">
      <header className="bg-gray-800 border-b border-gray-700 px-6 py-3 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate('/contests')} className="text-gray-400 hover:text-white">
            <ArrowLeft size={18} />
          </button>
          <h1 className="text-lg font-semibold">{isEditMode ? '编辑比赛' : '创建比赛'}</h1>
        </div>
        <UserMenu />
      </header>

      <div className="max-w-3xl mx-auto p-6 space-y-6">
        {pageLoading ? (
          <div className="flex items-center justify-center py-20 text-gray-500"><Loader2 className="animate-spin mr-2" size={18} />加载中...</div>
        ) : (
        <>
        {/* 基本信息 */}
        <div className="bg-gray-800 rounded-lg border border-gray-700 p-5 space-y-4">
          <h2 className="text-sm font-medium text-gray-300">基本信息</h2>
          <div>
            <label className="block text-xs text-gray-500 mb-1">比赛标题 *</label>
            <input value={title} onChange={(e) => setTitle(e.target.value)}
              className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-2 text-sm" placeholder="算法周赛 #1" />
          </div>
          <div>
            <label className="block text-xs text-gray-500 mb-1">比赛说明</label>
            <textarea value={description} onChange={(e) => setDescription(e.target.value)}
              className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-2 text-sm h-20 resize-none" placeholder="规则说明、注意事项等" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs text-gray-500 mb-1">比赛类型</label>
              <div className="flex gap-2">
                {([{ key: 'individual' as const, label: '个人赛' }, { key: 'team' as const, label: '组队赛' }]).map((t) => (
                  <button key={t.key} onClick={() => setContestType(t.key)}
                    className={`px-4 py-1.5 rounded text-sm ${contestType === t.key ? 'bg-blue-600 text-white' : 'bg-gray-700 text-gray-300'}`}>{t.label}</button>
                ))}
              </div>
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">计分规则</label>
              <select value={scoringRule} onChange={(e) => setScoringRule(e.target.value)}
                className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm">
                <option value="acm">ACM 罚时制</option>
                <option value="oi">OI 分数制</option>
                <option value="cf">CF 风格</option>
              </select>
            </div>
          </div>
        </div>

        {/* 时间设置 */}
        <div className="bg-gray-800 rounded-lg border border-gray-700 p-5 space-y-4">
          <h2 className="text-sm font-medium text-gray-300">时间设置</h2>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-xs text-gray-500 mb-1">开始日期 *</label>
              <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)}
                className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">开始时间 *</label>
              <input type="time" value={startTime} onChange={(e) => setStartTime(e.target.value)}
                className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">时长（分钟）*</label>
              <input type="number" value={duration} onChange={(e) => setDuration(Number(e.target.value))} min={10}
                className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs text-gray-500 mb-1">封榜时间（结束前N分钟，0=不封榜）</label>
              <input type="number" value={freezeMinutes} onChange={(e) => setFreezeMinutes(Number(e.target.value))} min={0}
                className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" />
            </div>
            {scoringRule === 'acm' && (
              <div>
                <label className="block text-xs text-gray-500 mb-1">ACM 罚时（每次错误罚N分钟）</label>
                <input type="number" value={penaltyTime} onChange={(e) => setPenaltyTime(Number(e.target.value))} min={0}
                  className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" />
              </div>
            )}
          </div>
        </div>

        {/* 参赛设置 */}
        <div className="bg-gray-800 rounded-lg border border-gray-700 p-5 space-y-4">
          <h2 className="text-sm font-medium text-gray-300">参赛设置</h2>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs text-gray-500 mb-1">最大参赛人数（0=不限）</label>
              <input type="number" value={maxParticipants} onChange={(e) => setMaxParticipants(Number(e.target.value))} min={0}
                className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" />
            </div>
            {contestType === 'team' && (
              <div>
                <label className="block text-xs text-gray-500 mb-1">每队最大人数</label>
                <input type="number" value={maxTeamSize} onChange={(e) => setMaxTeamSize(Number(e.target.value))} min={2} max={10}
                  className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" />
              </div>
            )}
          </div>
          <label className="flex items-center gap-2 text-sm text-gray-400">
            <input type="checkbox" checked={isPublic} onChange={(e) => setIsPublic(e.target.checked)}
              className="rounded bg-gray-700 border-gray-600" />
            公开比赛（所有人可见可报名）
          </label>
          {!isPublic && (
            <div>
              <label className="block text-xs text-gray-500 mb-1">比赛密码</label>
              <input value={password} onChange={(e) => setPassword(e.target.value)}
                className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" placeholder="参赛者需输入密码才能报名" />
            </div>
          )}
        </div>

        {/* 题目设置 */}
        <div className="bg-gray-800 rounded-lg border border-gray-700 p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-medium text-gray-300">
              题目设置 <span className="text-gray-500">（已选 {selectedProblems.length} 题）</span>
            </h2>
            <button onClick={openModal}
              className="flex items-center gap-1.5 px-3 py-1.5 bg-blue-600 text-white rounded text-sm hover:bg-blue-700">
              <Plus size={14} /> 选择题目
            </button>
          </div>

          {selectedProblems.length === 0 ? (
            <div className="text-center py-8 text-gray-500 text-sm">
              暂未选择题目，点击上方按钮选题
            </div>
          ) : (
            <div className="space-y-1">
              {selectedProblems.map((p, i) => (
                <div key={p.slug} className="flex items-center gap-2 bg-gray-750 rounded px-3 py-2 group hover:bg-gray-700">
                  <span className="text-gray-500 text-xs w-5">{String.fromCharCode(65 + i)}</span>
                  <div className="flex items-center gap-1">
                    <button onClick={() => moveProblem(i, -1)} disabled={i === 0}
                      className="text-gray-600 hover:text-gray-400 disabled:opacity-20 text-xs">▲</button>
                    <button onClick={() => moveProblem(i, 1)} disabled={i === selectedProblems.length - 1}
                      className="text-gray-600 hover:text-gray-400 disabled:opacity-20 text-xs">▼</button>
                  </div>
                  {p.frontendId && <span className="text-gray-500 text-xs">#{p.frontendId}</span>}
                  <span className="text-sm flex-1">{p.title}</span>
                  <span className={`text-xs px-1.5 py-0.5 rounded ${diffColor[p.difficulty] || 'text-gray-400'}`}>{p.difficulty}</span>
                  <input type="number" value={p.score} onChange={(e) => updateScore(p.slug, Number(e.target.value))}
                    className="w-16 bg-gray-700 border border-gray-600 rounded px-2 py-0.5 text-xs text-center" min={1} />
                  <span className="text-xs text-gray-500">分</span>
                  <button onClick={() => removeProblem(p.slug)}
                    className="text-gray-600 hover:text-red-400 opacity-0 group-hover:opacity-100 transition-opacity">
                    <Trash2 size={14} />
                  </button>
                </div>
              ))}
              <div className="text-xs text-gray-500 text-right pt-1">
                总分：{selectedProblems.reduce((sum, p) => sum + p.score, 0)}
              </div>
            </div>
          )}
        </div>

        {/* 提交按钮 */}
        <div className="flex justify-end gap-3">
          <button onClick={() => navigate('/contests')}
            className="px-5 py-2 bg-gray-700 text-gray-300 rounded-lg text-sm hover:bg-gray-600">取消</button>
          {isEditMode && (
            <button onClick={handleSaveAndPublish} disabled={publishing || submitting}
              className="px-5 py-2 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 disabled:opacity-50 flex items-center gap-2">
              {publishing && <Loader2 size={14} className="animate-spin" />}
              保存并发布
            </button>
          )}
          <button onClick={handleSubmit} disabled={submitting || publishing}
            className="px-5 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2">
            {submitting && <Loader2 size={14} className="animate-spin" />}
            {isEditMode ? '保存草稿' : '创建比赛（草稿）'}
          </button>
        </div>
        </>
        )}
      </div>

      {/* ========== 选题模态框 ========== */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60" onClick={() => setModalOpen(false)}>
          <div className="bg-gray-800 rounded-xl border border-gray-700 w-[800px] max-h-[85vh] flex flex-col"
            onClick={(e) => e.stopPropagation()}>
            {/* 模态框头部 */}
            <div className="flex items-center justify-between px-5 py-3 border-b border-gray-700">
              <h3 className="font-medium">选择比赛题目</h3>
              <button onClick={() => setModalOpen(false)} className="text-gray-500 hover:text-white"><X size={18} /></button>
            </div>

            {/* Tab 切换 */}
            <div className="flex gap-1 px-5 pt-3">
              {[
                { key: 'search' as const, icon: <Search size={14} />, label: '搜索选题' },
                { key: 'quick' as const, icon: <Zap size={14} />, label: '快速组题' },
                { key: 'smart' as const, icon: <Bot size={14} />, label: 'AI 组题' },
                { key: 'problemset' as const, icon: <Plus size={14} />, label: '导入题单' },
              ].map((t) => (
                <button key={t.key} onClick={() => setModalTab(t.key)}
                  className={`flex items-center gap-1 px-3 py-1.5 rounded-t text-sm ${
                    modalTab === t.key ? 'bg-gray-700 text-white' : 'text-gray-500 hover:text-gray-300'
                  }`}>{t.icon} {t.label}</button>
              ))}
            </div>

            {/* 模态框内容 */}
            <div className="flex-1 overflow-y-auto p-5">
              {/* ====== 搜索选题 ====== */}
              {modalTab === 'search' && (
                <div className="space-y-3">
                  <div className="flex gap-2">
                    <div className="relative flex-1">
                      <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
                      <input value={searchKeyword} onChange={(e) => setSearchKeyword(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleSearch(1)}
                        className="w-full bg-gray-700 border border-gray-600 rounded pl-9 pr-3 py-1.5 text-sm"
                        placeholder="搜索题号或标题..." />
                    </div>
                    <select value={searchDifficulty} onChange={(e) => { setSearchDifficulty(e.target.value); setTimeout(() => handleSearch(1), 0) }}
                      className="bg-gray-700 border border-gray-600 rounded px-2 py-1.5 text-sm">
                      <option value="">全部难度</option>
                      <option value="Easy">Easy</option>
                      <option value="Medium">Medium</option>
                      <option value="Hard">Hard</option>
                    </select>
                    <button onClick={() => handleSearch(1)}
                      className="px-3 py-1.5 bg-blue-600 text-white rounded text-sm hover:bg-blue-700">搜索</button>
                  </div>

                  {searchLoading ? (
                    <div className="flex items-center justify-center py-8 text-gray-500"><Loader2 className="animate-spin mr-2" size={16} />搜索中...</div>
                  ) : (
                    <div className="space-y-1">
                      {searchResults.map((p) => {
                        const isSelected = selectedProblems.some((sp) => sp.slug === p.slug)
                        return (
                          <div key={p.id} className="flex items-center justify-between px-3 py-2 rounded hover:bg-gray-700 text-sm">
                            <div className="flex items-center gap-2">
                              <span className="text-gray-500 w-10">{p.frontendId}</span>
                              <span>{p.title}</span>
                              <span className={`text-xs px-1.5 py-0.5 rounded ${diffColor[p.difficulty] || ''}`}>{p.difficulty}</span>
                            </div>
                            <button onClick={() => addProblem(p)} disabled={isSelected}
                              className={`px-2 py-1 rounded text-xs ${
                                isSelected ? 'bg-gray-700 text-gray-500 cursor-not-allowed' : 'bg-blue-600/20 text-blue-400 hover:bg-blue-600/30'
                              }`}>
                              {isSelected ? '已选' : '+ 添加'}
                            </button>
                          </div>
                        )
                      })}
                    </div>
                  )}

                  {searchTotalPages > 1 && (
                    <div className="flex items-center justify-center gap-2 pt-2">
                      <button onClick={() => handleSearch(1)} disabled={searchPage <= 1}
                        className="text-xs text-gray-400 hover:text-white disabled:opacity-30">首页</button>
                      <button onClick={() => handleSearch(searchPage - 1)} disabled={searchPage <= 1}
                        className="text-xs text-gray-400 hover:text-white disabled:opacity-30">&lt;</button>
                      <input type="number" value={searchPage} min={1} max={searchTotalPages}
                        onChange={(e) => { const p = Number(e.target.value); if (p >= 1 && p <= searchTotalPages) handleSearch(p) }}
                        className="w-12 bg-gray-700 border border-gray-600 rounded px-1 py-0.5 text-xs text-center [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none" />
                      <span className="text-xs text-gray-500">/ {searchTotalPages}</span>
                      <button onClick={() => handleSearch(searchPage + 1)} disabled={searchPage >= searchTotalPages}
                        className="text-xs text-gray-400 hover:text-white disabled:opacity-30">&gt;</button>
                      <button onClick={() => handleSearch(searchTotalPages)} disabled={searchPage >= searchTotalPages}
                        className="text-xs text-gray-400 hover:text-white disabled:opacity-30">末页</button>
                    </div>
                  )}
                </div>
              )}

              {/* ====== 快速组题 ====== */}
              {modalTab === 'quick' && (
                <div className="space-y-4">
                  <p className="text-xs text-gray-500">按难度分布随机抽取题目，替换当前已选题目</p>
                  <div className="flex gap-4">
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">题目数量</label>
                      <div className="flex gap-1">
                        {[3, 5, 7, 10].map((n) => (
                          <button key={n} onClick={() => setQuickCount(n)}
                            className={`px-3 py-1 rounded text-sm ${quickCount === n ? 'bg-blue-600 text-white' : 'bg-gray-700 text-gray-300'}`}>{n}</button>
                        ))}
                      </div>
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">难度</label>
                      <div className="flex gap-1">
                        {[{ k: 'beginner', l: '入门' }, { k: 'intermediate', l: '进阶' }, { k: 'advanced', l: '挑战' }].map((d) => (
                          <button key={d.k} onClick={() => setQuickDiff(d.k)}
                            className={`px-3 py-1 rounded text-sm ${quickDiff === d.k ? 'bg-blue-600 text-white' : 'bg-gray-700 text-gray-300'}`}>{d.l}</button>
                        ))}
                      </div>
                    </div>
                  </div>
                  <button onClick={handleQuickGenerate} disabled={quickLoading}
                    className="px-4 py-2 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2">
                    {quickLoading ? <Loader2 size={14} className="animate-spin" /> : <Zap size={14} />}
                    {quickLoading ? '组题中...' : '一键组题'}
                  </button>
                </div>
              )}

              {/* ====== AI 智能组题 ====== */}
              {modalTab === 'smart' && (
                <div className="space-y-3">
                  <p className="text-xs text-gray-500">AI 根据你描述的水平和目标，从知识库中智能选题</p>
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">题目数量</label>
                      <input type="number" value={smartCount} onChange={(e) => setSmartCount(Number(e.target.value))}
                        min={1} max={20} className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">偏好方向（可选）</label>
                      <input value={smartPref} onChange={(e) => setSmartPref(e.target.value)}
                        className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" placeholder="如：动态规划" />
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs text-gray-500 mb-1">参赛者水平描述 *</label>
                    <textarea value={smartSelf} onChange={(e) => setSmartSelf(e.target.value)}
                      className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-2 text-sm h-14 resize-none"
                      placeholder="如：刷了50题Easy，Medium偶尔能做出来" />
                  </div>
                  <div>
                    <label className="block text-xs text-gray-500 mb-1">比赛目标难度 *</label>
                    <textarea value={smartGoal} onChange={(e) => setSmartGoal(e.target.value)}
                      className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-2 text-sm h-14 resize-none"
                      placeholder="如：希望比赛难度适中，有区分度" />
                  </div>
                  <button onClick={handleSmartGenerate} disabled={smartLoading}
                    className="px-4 py-2 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2">
                    {smartLoading ? <Loader2 size={14} className="animate-spin" /> : <Bot size={14} />}
                    {smartLoading ? 'AI 组题中...' : 'AI 智能组题'}
                  </button>
                  {smartResponse && (
                    <div className="bg-gray-900 border border-gray-700 rounded p-3 max-h-48 overflow-y-auto">
                      <pre className="text-xs text-gray-400 whitespace-pre-wrap font-sans">{smartResponse}</pre>
                    </div>
                  )}
                </div>
              )}

              {/* ====== 导入题单 ====== */}
              {modalTab === 'problemset' && (
                <div className="space-y-3">
                  <p className="text-xs text-gray-500">从已有的题单中导入题目，替换当前已选题目</p>
                  {psLoading ? (
                    <div className="flex items-center justify-center py-8 text-gray-500"><Loader2 className="animate-spin mr-2" size={16} />加载中...</div>
                  ) : problemSets.length === 0 ? (
                    <div className="text-center py-8 text-gray-500 text-sm">暂无题单，请先在「智能组题中心」创建</div>
                  ) : (
                    <div className="space-y-1">
                      {problemSets.map((ps) => (
                        <div key={ps.id} className="flex items-center justify-between px-3 py-2.5 rounded hover:bg-gray-700">
                          <div>
                            <span className="text-sm font-medium">{ps.title}</span>
                            <span className="text-xs text-gray-500 ml-2">{ps.problemCount} 题</span>
                            <span className="text-xs text-gray-600 ml-2">
                              {ps.sourceType === 'quick' ? '快速组题' : ps.sourceType === 'dify_smart' ? 'AI组题' : '手动'}
                            </span>
                          </div>
                          <button onClick={() => handleImportProblemSet(ps.id)}
                            className="px-3 py-1 bg-blue-600/20 text-blue-400 rounded text-xs hover:bg-blue-600/30">
                            导入
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* 模态框底部：已选题目预览 */}
            <div className="border-t border-gray-700 px-5 py-3">
              <div className="flex items-center justify-between">
                <span className="text-xs text-gray-500">
                  已选 {selectedProblems.length} 题
                  {selectedProblems.length > 0 && (
                    <span className="ml-2">
                      ({selectedProblems.map((p) => p.slug.length > 15 ? p.slug.slice(0, 15) + '...' : p.slug).join(', ')})
                    </span>
                  )}
                </span>
                <button onClick={() => setModalOpen(false)}
                  className="px-4 py-1.5 bg-blue-600 text-white rounded text-sm hover:bg-blue-700">
                  确认 ({selectedProblems.length} 题)
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
