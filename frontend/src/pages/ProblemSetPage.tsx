import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Zap, Pencil, Bot, Trash2, Play, ChevronLeft, ChevronRight, Loader2, Search, X, Plus } from 'lucide-react'
import { getMyProblemSets, quickGenerate, deleteProblemSet, getProblemSetItems, createProblemSet } from '@/api/problemSet'
import { getProblems } from '@/api/problem'
import { getUserProfile, analyzeUserProfile } from '@/api/userProfile'
import { smartGenerateStream } from '@/api/dify'
import { toast, confirm } from '@/store/uiStore'
import UserMenu from '@/components/UserMenu'
import type { ProblemSet, ProblemSetItemDetail, QuickGenerateParams, UserProfile, Problem } from '@/types'

/** 难度标签颜色 */
const diffColor: Record<string, string> = {
  Easy: 'text-green-400 bg-green-900/30',
  EASY: 'text-green-400 bg-green-900/30',
  Medium: 'text-yellow-400 bg-yellow-900/30',
  MEDIUM: 'text-yellow-400 bg-yellow-900/30',
  Hard: 'text-red-400 bg-red-900/30',
  HARD: 'text-red-400 bg-red-900/30',
}

const sourceIcon: Record<string, React.ReactNode> = {
  manual: <Pencil size={14} />,
  quick: <Zap size={14} />,
  dify_smart: <Bot size={14} />,
}
const sourceLabel: Record<string, string> = {
  manual: '手动',
  quick: '快速',
  dify_smart: '智能',
}

/** 手动选题中的已选题目 */
interface PickedProblem {
  slug: string
  title: string
  frontendId: string
  difficulty: string
  score: number
}

export default function ProblemSetPage() {
  const navigate = useNavigate()
  const [tab, setTab] = useState<'quick' | 'manual' | 'smart'>('quick')
  const [sets, setSets] = useState<ProblemSet[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [loading, setLoading] = useState(false)
  const [generating, setGenerating] = useState(false)
  const [profile, setProfile] = useState<UserProfile | null>(null)

  // 快速组题
  const [count, setCount] = useState(10)
  const [diffLevel, setDiffLevel] = useState('intermediate')
  const [excludeSolved, setExcludeSolved] = useState(true)

  // 智能组题
  const [smartCount, setSmartCount] = useState(10)
  const [selfAssessment, setSelfAssessment] = useState('')
  const [targetGoal, setTargetGoal] = useState('')
  const [preference, setPreference] = useState('')
  const [smartGenerating, setSmartGenerating] = useState(false)
  const [smartResponse, setSmartResponse] = useState('')

  // 手动选题
  const [manualTitle, setManualTitle] = useState('')
  const [pickedProblems, setPickedProblems] = useState<PickedProblem[]>([])
  const [modalOpen, setModalOpen] = useState(false)
  const [searchKeyword, setSearchKeyword] = useState('')
  const [searchDifficulty, setSearchDifficulty] = useState('')
  const [searchResults, setSearchResults] = useState<Problem[]>([])
  const [searchLoading, setSearchLoading] = useState(false)
  const [searchTotal, setSearchTotal] = useState(0)
  const [searchPage, setSearchPage] = useState(1)
  const [manualSaving, setManualSaving] = useState(false)

  // 展开的题单详情
  const [expandedSetId, setExpandedSetId] = useState<number | null>(null)
  const [expandedItems, setExpandedItems] = useState<ProblemSetItemDetail[]>([])

  useEffect(() => { loadSets(); loadProfile() }, [page])

  const loadSets = async () => {
    setLoading(true)
    try {
      const res = await getMyProblemSets(page, 10)
      if (res.code === 200) { setSets(res.data.records); setTotal(res.data.total) }
    } catch {}
    setLoading(false)
  }

  const loadProfile = async () => {
    try {
      const res = await getUserProfile()
      if (res.code === 200) setProfile(res.data)
    } catch {}
  }

  // 快速组题
  const handleQuickGenerate = async () => {
    setGenerating(true)
    try {
      const res = await quickGenerate({ count, difficultyLevel: diffLevel, excludeSolved })
      if (res.code === 200) loadSets()
    } catch (e: any) { toast.error(e.message || '组题失败') }
    setGenerating(false)
  }

  // 智能组题
  const handleSmartGenerate = () => {
    if (!selfAssessment.trim() || !targetGoal.trim()) { toast.warning('请填写你的水平描述和目标'); return }
    setSmartGenerating(true)
    setSmartResponse('')
    smartGenerateStream(
      { count: smartCount, selfAssessment, targetGoal, preference: preference || undefined },
      {
        onChunk: (chunk) => setSmartResponse((prev) => prev + chunk),
        onDone: () => { setSmartGenerating(false); loadSets() },
        onError: (err) => { setSmartGenerating(false); toast.error('智能组题失败：' + err) },
      },
    ).catch((e) => { setSmartGenerating(false); toast.error(e.message) })
  }

  // 搜索题目
  const handleSearch = async (pg = 1) => {
    setSearchLoading(true)
    setSearchPage(pg)
    try {
      const res = await getProblems({ keyword: searchKeyword || undefined, difficulty: searchDifficulty || undefined, pageNum: pg, pageSize: 10 })
      if (res.code === 200) { setSearchResults(res.data.records); setSearchTotal(res.data.total) }
    } catch {}
    setSearchLoading(false)
  }

  const openModal = () => { setModalOpen(true); handleSearch(1) }

  const addProblem = (p: Problem) => {
    if (pickedProblems.some((pp) => pp.slug === p.slug)) return
    setPickedProblems((prev) => [...prev, { slug: p.slug, title: p.title, frontendId: p.frontendId || '', difficulty: p.difficulty, score: 100 }])
  }

  const removePicked = (slug: string) => setPickedProblems((prev) => prev.filter((p) => p.slug !== slug))

  const movePickedProblem = (index: number, dir: -1 | 1) => {
    const ni = index + dir
    if (ni < 0 || ni >= pickedProblems.length) return
    const arr = [...pickedProblems]
    ;[arr[index], arr[ni]] = [arr[ni], arr[index]]
    setPickedProblems(arr)
  }

  // 保存手动选题为题单
  const handleSaveManual = async () => {
    if (pickedProblems.length === 0) { toast.warning('请至少添加一道题目'); return }
    const title = manualTitle.trim() || `手动题单 (${pickedProblems.length}题)`
    setManualSaving(true)
    try {
      const res = await createProblemSet({
        title,
        problems: pickedProblems.map((p) => ({ slug: p.slug, score: p.score })),
      })
      if (res.code === 200) {
        setPickedProblems([])
        setManualTitle('')
        loadSets()
      }
    } catch (e: any) { toast.error(e.message || '保存失败') }
    setManualSaving(false)
  }

  const handleDelete = async (id: number) => {
    if (!await confirm('确定要删除这个题单吗？', { type: 'danger', confirmText: '删除' })) return
    try { await deleteProblemSet(id); loadSets() } catch {}
  }

  const handleExpand = async (setId: number) => {
    if (expandedSetId === setId) { setExpandedSetId(null); return }
    try {
      const res = await getProblemSetItems(setId)
      if (res.code === 200) { setExpandedItems(res.data); setExpandedSetId(setId) }
    } catch {}
  }

  const handleAnalyze = async () => {
    try { const res = await analyzeUserProfile(); if (res.code === 200) setProfile(res.data) } catch {}
  }

  const totalPages = Math.ceil(total / 10)
  const searchTotalPages = Math.ceil(searchTotal / 10)

  return (
    <div className="min-h-screen bg-gray-900 text-gray-100">
      {/* 顶部导航 */}
      <header className="bg-gray-800 border-b border-gray-700 px-6 py-3 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate('/')} className="text-gray-400 hover:text-white text-sm">&larr; 返回题库</button>
          <h1 className="text-lg font-semibold">智能组题中心</h1>
        </div>
        <UserMenu />
      </header>

      <div className="max-w-5xl mx-auto p-6 space-y-6">
        {/* 用户画像 */}
        {profile && (
          <div className="bg-gray-800 rounded-lg p-4 border border-gray-700">
            <div className="flex items-center justify-between mb-2">
              <h2 className="text-sm font-medium text-gray-300">我的画像</h2>
              <button onClick={handleAnalyze} className="text-xs text-blue-400 hover:text-blue-300">刷新分析</button>
            </div>
            <div className="flex gap-6 text-sm">
              <div><span className="text-gray-500">水平：</span><span className="text-green-400">{profile.skillLevel}</span></div>
              <div><span className="text-gray-500">Easy：</span>{profile.solvedEasy}</div>
              <div><span className="text-gray-500">Medium：</span>{profile.solvedMedium}</div>
              <div><span className="text-gray-500">Hard：</span>{profile.solvedHard}</div>
              <div><span className="text-gray-500">通过率：</span>{profile.acceptanceRate}%</div>
            </div>
          </div>
        )}

        {/* Tab 选择 */}
        <div className="flex gap-2">
          {[
            { key: 'quick' as const, icon: <Zap size={16} />, label: '快速组题' },
            { key: 'manual' as const, icon: <Pencil size={16} />, label: '手动选题' },
            { key: 'smart' as const, icon: <Bot size={16} />, label: '智能组题' },
          ].map((t) => (
            <button key={t.key} onClick={() => setTab(t.key)}
              className={`flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                tab === t.key ? 'bg-blue-600 text-white' : 'bg-gray-800 text-gray-400 hover:text-white border border-gray-700'
              }`}>{t.icon} {t.label}</button>
          ))}
        </div>

        {/* ====== 快速组题 ====== */}
        {tab === 'quick' && (
          <div className="bg-gray-800 rounded-lg p-5 border border-gray-700 space-y-4">
            <h3 className="text-sm font-medium text-gray-300">快速组题</h3>
            <div className="flex flex-wrap gap-4">
              <div>
                <label className="block text-xs text-gray-500 mb-1">题目数量</label>
                <div className="flex gap-2">
                  {[5, 10, 15, 20].map((n) => (
                    <button key={n} onClick={() => setCount(n)}
                      className={`px-3 py-1 rounded text-sm ${count === n ? 'bg-blue-600 text-white' : 'bg-gray-700 text-gray-300 hover:bg-gray-600'}`}>{n}</button>
                  ))}
                </div>
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">难度定位</label>
                <div className="flex gap-2">
                  {[{ key: 'beginner', label: '入门级' }, { key: 'intermediate', label: '进阶级' }, { key: 'advanced', label: '挑战级' }].map((d) => (
                    <button key={d.key} onClick={() => setDiffLevel(d.key)}
                      className={`px-3 py-1 rounded text-sm ${diffLevel === d.key ? 'bg-blue-600 text-white' : 'bg-gray-700 text-gray-300 hover:bg-gray-600'}`}>{d.label}</button>
                  ))}
                </div>
              </div>
            </div>
            <label className="flex items-center gap-2 text-sm text-gray-400">
              <input type="checkbox" checked={excludeSolved} onChange={(e) => setExcludeSolved(e.target.checked)} className="rounded bg-gray-700 border-gray-600" />
              排除已 AC 的题
            </label>
            <button onClick={handleQuickGenerate} disabled={generating}
              className="px-5 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2">
              {generating && <Loader2 size={14} className="animate-spin" />} 生成题单
            </button>
          </div>
        )}

        {/* ====== 手动选题 ====== */}
        {tab === 'manual' && (
          <div className="bg-gray-800 rounded-lg p-5 border border-gray-700 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-medium text-gray-300">
                手动选题 <span className="text-gray-500">（已选 {pickedProblems.length} 题）</span>
              </h3>
              <button onClick={openModal}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-blue-600 text-white rounded text-sm hover:bg-blue-700">
                <Search size={14} /> 搜索添加题目
              </button>
            </div>

            {/* 题单标题 */}
            <div>
              <label className="block text-xs text-gray-500 mb-1">题单标题（可选）</label>
              <input value={manualTitle} onChange={(e) => setManualTitle(e.target.value)}
                className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" placeholder="如：DP 专题练习" />
            </div>

            {/* 已选题目列表 */}
            {pickedProblems.length === 0 ? (
              <div className="text-center py-8 text-gray-500 text-sm">暂未选择题目，点击上方按钮搜索添加</div>
            ) : (
              <div className="space-y-1">
                {pickedProblems.map((p, i) => (
                  <div key={p.slug} className="flex items-center gap-2 bg-gray-750 rounded px-3 py-2 group hover:bg-gray-700">
                    <span className="text-gray-500 text-xs w-5">{i + 1}</span>
                    <div className="flex items-center gap-1">
                      <button onClick={() => movePickedProblem(i, -1)} disabled={i === 0}
                        className="text-gray-600 hover:text-gray-400 disabled:opacity-20 text-xs">▲</button>
                      <button onClick={() => movePickedProblem(i, 1)} disabled={i === pickedProblems.length - 1}
                        className="text-gray-600 hover:text-gray-400 disabled:opacity-20 text-xs">▼</button>
                    </div>
                    {p.frontendId && <span className="text-gray-500 text-xs">#{p.frontendId}</span>}
                    <span className="text-sm flex-1">{p.title}</span>
                    <span className={`text-xs px-1.5 py-0.5 rounded ${diffColor[p.difficulty] || 'text-gray-400'}`}>{p.difficulty}</span>
                    <input type="number" value={p.score}
                      onChange={(e) => setPickedProblems((prev) => prev.map((pp) => pp.slug === p.slug ? { ...pp, score: Number(e.target.value) } : pp))}
                      className="w-16 bg-gray-700 border border-gray-600 rounded px-2 py-0.5 text-xs text-center" min={1} />
                    <span className="text-xs text-gray-500">分</span>
                    <button onClick={() => removePicked(p.slug)}
                      className="text-gray-600 hover:text-red-400 opacity-0 group-hover:opacity-100 transition-opacity">
                      <Trash2 size={14} />
                    </button>
                  </div>
                ))}
                <div className="text-xs text-gray-500 text-right pt-1">
                  总分：{pickedProblems.reduce((s, p) => s + p.score, 0)}
                </div>
              </div>
            )}

            {/* 保存按钮 */}
            {pickedProblems.length > 0 && (
              <button onClick={handleSaveManual} disabled={manualSaving}
                className="px-5 py-2 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 disabled:opacity-50 flex items-center gap-2">
                {manualSaving ? <Loader2 size={14} className="animate-spin" /> : <Plus size={14} />}
                保存为题单 ({pickedProblems.length} 题)
              </button>
            )}
          </div>
        )}

        {/* ====== 智能组题 ====== */}
        {tab === 'smart' && (
          <div className="bg-gray-800 rounded-lg p-5 border border-gray-700 space-y-4">
            <h3 className="text-sm font-medium text-gray-300">Dify 智能组题</h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs text-gray-500 mb-1">题目数量</label>
                <input type="number" value={smartCount} onChange={(e) => setSmartCount(Number(e.target.value))} min={1} max={50}
                  className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" />
              </div>
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">我的水平（描述你当前的刷题情况）</label>
              <textarea value={selfAssessment} onChange={(e) => setSelfAssessment(e.target.value)} placeholder="例如：刷了50题Easy，Medium偶尔能做出来"
                className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-2 text-sm h-16 resize-none" />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">我的目标（想要达到的水平）</label>
              <textarea value={targetGoal} onChange={(e) => setTargetGoal(e.target.value)} placeholder="例如：想要能稳定做出Medium，冲击Hard"
                className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-2 text-sm h-16 resize-none" />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">偏好方向（可选）</label>
              <input value={preference} onChange={(e) => setPreference(e.target.value)} placeholder="例如：想加强动态规划"
                className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm" />
            </div>
            <button onClick={handleSmartGenerate} disabled={smartGenerating}
              className="px-5 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2">
              {smartGenerating ? <Loader2 size={14} className="animate-spin" /> : <Bot size={14} />}
              {smartGenerating ? 'AI 正在组题...' : 'AI 组题'}
            </button>
            {smartResponse && (
              <div className="mt-4 bg-gray-900 border border-gray-700 rounded-lg p-4 max-h-80 overflow-y-auto">
                <h4 className="text-xs text-gray-500 mb-2">AI 分析结果</h4>
                <pre className="text-sm text-gray-300 whitespace-pre-wrap font-sans">{smartResponse}</pre>
              </div>
            )}
          </div>
        )}

        {/* ====== 我的题单列表 ====== */}
        <div>
          <h2 className="text-base font-medium mb-3">我的题单</h2>
          {loading ? (
            <div className="flex items-center justify-center py-12 text-gray-500"><Loader2 className="animate-spin mr-2" size={18} /> 加载中...</div>
          ) : sets.length === 0 ? (
            <div className="text-center py-12 text-gray-500 text-sm">暂无题单，快去组一套吧</div>
          ) : (
            <div className="space-y-2">
              {sets.map((ps) => (
                <div key={ps.id} className="bg-gray-800 rounded-lg border border-gray-700">
                  <div className="flex items-center justify-between px-4 py-3 cursor-pointer hover:bg-gray-750" onClick={() => handleExpand(ps.id)}>
                    <div className="flex items-center gap-3">
                      <span className="text-gray-500">{sourceIcon[ps.sourceType]}</span>
                      <span className="font-medium text-sm">{ps.title}</span>
                      <span className="text-xs text-gray-500">{ps.problemCount} 题</span>
                      <span className="text-xs text-gray-500">{sourceLabel[ps.sourceType]}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <button onClick={(e) => { e.stopPropagation(); handleExpand(ps.id).then(() => { if (expandedItems[0]) navigate(`/problem/${expandedItems[0].slug}`) }) }}
                        className="text-xs px-2 py-1 bg-green-600/20 text-green-400 rounded hover:bg-green-600/30">
                        <Play size={12} className="inline mr-1" />练习
                      </button>
                      <button onClick={(e) => { e.stopPropagation(); handleDelete(ps.id) }}
                        className="text-xs px-2 py-1 bg-red-600/20 text-red-400 rounded hover:bg-red-600/30">
                        <Trash2 size={12} />
                      </button>
                    </div>
                  </div>
                  {expandedSetId === ps.id && (
                    <div className="border-t border-gray-700 px-4 py-2">
                      {expandedItems.length === 0 ? (
                        <p className="text-xs text-gray-500 py-2">题单为空</p>
                      ) : (
                        <table className="w-full text-sm">
                          <thead>
                            <tr className="text-gray-500 text-xs">
                              <th className="text-left py-1 w-10">#</th>
                              <th className="text-left py-1">题号</th>
                              <th className="text-left py-1">标题</th>
                              <th className="text-left py-1">难度</th>
                              <th className="text-right py-1">分值</th>
                            </tr>
                          </thead>
                          <tbody>
                            {expandedItems.map((item) => (
                              <tr key={item.id} className="hover:bg-gray-750 cursor-pointer" onClick={() => navigate(`/problem/${item.slug}`)}>
                                <td className="py-1 text-gray-500">{item.seqOrder}</td>
                                <td className="py-1 text-gray-400">{item.frontendId}</td>
                                <td className="py-1">{item.title}</td>
                                <td className="py-1">
                                  <span className={`px-1.5 py-0.5 rounded text-xs ${diffColor[item.difficulty] || 'text-gray-400'}`}>{item.difficulty}</span>
                                </td>
                                <td className="py-1 text-right text-gray-400">{item.score}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      )}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}

          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mt-4">
              <button onClick={() => setPage(Math.max(1, page - 1))} disabled={page === 1} className="p-1 text-gray-400 hover:text-white disabled:opacity-30"><ChevronLeft size={18} /></button>
              <span className="text-sm text-gray-400">{page} / {totalPages}</span>
              <button onClick={() => setPage(Math.min(totalPages, page + 1))} disabled={page === totalPages} className="p-1 text-gray-400 hover:text-white disabled:opacity-30"><ChevronRight size={18} /></button>
            </div>
          )}
        </div>
      </div>

      {/* ========== 搜索选题模态框 ========== */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60" onClick={() => setModalOpen(false)}>
          <div className="bg-gray-800 rounded-xl border border-gray-700 w-[700px] max-h-[80vh] flex flex-col"
            onClick={(e) => e.stopPropagation()}>
            {/* 头部 */}
            <div className="flex items-center justify-between px-5 py-3 border-b border-gray-700">
              <h3 className="font-medium">搜索题目</h3>
              <button onClick={() => setModalOpen(false)} className="text-gray-500 hover:text-white"><X size={18} /></button>
            </div>

            {/* 搜索栏 */}
            <div className="px-5 pt-4 pb-2 flex gap-2">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
                <input value={searchKeyword} onChange={(e) => setSearchKeyword(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch(1)}
                  className="w-full bg-gray-700 border border-gray-600 rounded pl-9 pr-3 py-1.5 text-sm" placeholder="搜索题号或标题..." />
              </div>
              <select value={searchDifficulty} onChange={(e) => { setSearchDifficulty(e.target.value); setTimeout(() => handleSearch(1), 0) }}
                className="bg-gray-700 border border-gray-600 rounded px-2 py-1.5 text-sm">
                <option value="">全部难度</option>
                <option value="Easy">Easy</option>
                <option value="Medium">Medium</option>
                <option value="Hard">Hard</option>
              </select>
              <button onClick={() => handleSearch(1)} className="px-3 py-1.5 bg-blue-600 text-white rounded text-sm hover:bg-blue-700">搜索</button>
            </div>

            {/* 结果列表 */}
            <div className="flex-1 overflow-y-auto px-5 pb-2">
              {searchLoading ? (
                <div className="flex items-center justify-center py-8 text-gray-500"><Loader2 className="animate-spin mr-2" size={16} />搜索中...</div>
              ) : (
                <div className="space-y-1">
                  {searchResults.map((p) => {
                    const isSelected = pickedProblems.some((pp) => pp.slug === p.slug)
                    return (
                      <div key={p.id} className="flex items-center justify-between px-3 py-2 rounded hover:bg-gray-700 text-sm">
                        <div className="flex items-center gap-2">
                          <span className="text-gray-500 w-10">{p.frontendId}</span>
                          <span>{p.title}</span>
                          <span className={`text-xs px-1.5 py-0.5 rounded ${diffColor[p.difficulty] || ''}`}>{p.difficulty}</span>
                        </div>
                        <button onClick={() => addProblem(p)} disabled={isSelected}
                          className={`px-2 py-1 rounded text-xs ${isSelected ? 'bg-gray-700 text-gray-500 cursor-not-allowed' : 'bg-blue-600/20 text-blue-400 hover:bg-blue-600/30'}`}>
                          {isSelected ? '已选' : '+ 添加'}
                        </button>
                      </div>
                    )
                  })}
                </div>
              )}

              {searchTotalPages > 1 && (
                <div className="flex items-center justify-center gap-2 py-2">
                  <button onClick={() => handleSearch(1)} disabled={searchPage <= 1} className="text-xs text-gray-400 hover:text-white disabled:opacity-30">首页</button>
                  <button onClick={() => handleSearch(searchPage - 1)} disabled={searchPage <= 1} className="text-xs text-gray-400 hover:text-white disabled:opacity-30">&lt;</button>
                  <input type="number" value={searchPage} min={1} max={searchTotalPages}
                    onChange={(e) => { const p = Number(e.target.value); if (p >= 1 && p <= searchTotalPages) handleSearch(p) }}
                    className="w-12 bg-gray-700 border border-gray-600 rounded px-1 py-0.5 text-xs text-center [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none" />
                  <span className="text-xs text-gray-500">/ {searchTotalPages}</span>
                  <button onClick={() => handleSearch(searchPage + 1)} disabled={searchPage >= searchTotalPages} className="text-xs text-gray-400 hover:text-white disabled:opacity-30">&gt;</button>
                  <button onClick={() => handleSearch(searchTotalPages)} disabled={searchPage >= searchTotalPages} className="text-xs text-gray-400 hover:text-white disabled:opacity-30">末页</button>
                </div>
              )}
            </div>

            {/* 底部 */}
            <div className="border-t border-gray-700 px-5 py-3 flex items-center justify-between">
              <span className="text-xs text-gray-500">已选 {pickedProblems.length} 题</span>
              <button onClick={() => setModalOpen(false)} className="px-4 py-1.5 bg-blue-600 text-white rounded text-sm hover:bg-blue-700">
                确认 ({pickedProblems.length} 题)
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
