import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Loader2, Plus, Search, Users } from 'lucide-react'
import { listIndTeams, createIndTeam, applyToJoinTeam } from '@/api/team'
import UserMenu from '@/components/UserMenu'
import ThemeToggle from '@/components/ThemeToggle'
import ContestSidebar from '@/components/ContestSidebar'
import { toast } from '@/store/uiStore'
import type { IndTeamListItem } from '@/types'

export default function TeamSpacePage() {
  const navigate = useNavigate()
  const [teams, setTeams] = useState<IndTeamListItem[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [keyword, setKeyword] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [loading, setLoading] = useState(true)

  // 创建队伍
  const [showCreate, setShowCreate] = useState(false)
  const [newName, setNewName] = useState('')
  const [newDesc, setNewDesc] = useState('')

  useEffect(() => { void loadTeams() }, [page, keyword])

  const loadTeams = async () => {
    setLoading(true)
    try {
      const res = await listIndTeams(page, 20, keyword)
      if (res.code === 200) { setTeams(res.data.records); setTotal(res.data.total) }
    } catch {}
    setLoading(false)
  }

  const handleCreate = async () => {
    if (!newName.trim()) { toast.warning('请输入队伍名称'); return }
    try {
      const res = await createIndTeam({ teamName: newName.trim(), description: newDesc.trim() || undefined })
      if (res.code === 200) {
        toast.success('队伍创建成功')
        setNewName(''); setNewDesc(''); setShowCreate(false)
        void loadTeams()
      }
    } catch (e: any) { toast.error(e.message) }
  }

  const handleApply = async (teamId: number) => {
    try {
      await applyToJoinTeam(teamId)
      toast.success('申请已提交，等待队长审批')
    } catch (e: any) { toast.error(e.message) }
  }

  const totalPages = Math.ceil(total / 20)

  return (
    <div className="h-screen flex flex-col theme-bg-gradient overflow-hidden">
      <header className="theme-header px-6 py-3 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate('/')} className="theme-button-ghost text-sm">&larr; 返回题库</button>
          <h1 className="text-lg font-semibold flex items-center gap-2 theme-text">
            <Users size={20} style={{ color: 'var(--accent)' }} />
            队伍空间
          </h1>
        </div>
        <div className="flex items-center gap-2">
          <ThemeToggle />
          <UserMenu />
        </div>
      </header>

      <div className="flex flex-1 overflow-hidden">
        <ContestSidebar />
        <main className="flex-1 overflow-y-auto p-6 space-y-4">
          {/* 操作栏 */}
          <div className="flex flex-col lg:flex-row gap-3">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 theme-hint" />
              <input value={searchInput} onChange={(e) => setSearchInput(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && (() => { setKeyword(searchInput.trim()); setPage(1) })()}
                className="w-full theme-input rounded-xl pl-10 pr-3 py-2.5 text-sm" placeholder="搜索队伍名称" />
            </div>
            <div className="flex gap-2">
              <button onClick={() => setShowCreate(!showCreate)} className="theme-button-blue rounded-xl px-3 py-2 text-sm inline-flex items-center gap-1">
                <Plus size={14} />创建队伍
              </button>
            </div>
          </div>

          {/* 创建表单 */}
          {showCreate && (
            <div className="theme-card rounded-2xl p-4 space-y-3">
              <div className="text-sm font-medium theme-text">创建新队伍</div>
              <input value={newName} onChange={(e) => setNewName(e.target.value)} placeholder="队伍名称"
                className="w-full theme-input rounded-xl px-3 py-2 text-sm" />
              <textarea value={newDesc} onChange={(e) => setNewDesc(e.target.value)} placeholder="队伍描述（选填）"
                className="w-full theme-input rounded-xl px-3 py-2 text-sm resize-none" rows={2} maxLength={500} />
              <div className="flex gap-2">
                <button onClick={handleCreate} className="theme-button-blue rounded-xl px-4 py-2 text-sm">创建</button>
                <button onClick={() => setShowCreate(false)} className="theme-button-ghost rounded-xl px-4 py-2 text-sm">取消</button>
              </div>
            </div>
          )}

          <div className="text-xs theme-faint">共 {total} 支队伍</div>

          {/* 队伍列表 */}
          {loading ? (
            <div className="flex items-center justify-center py-20 theme-faint"><Loader2 className="animate-spin mr-2" size={18} />加载中...</div>
          ) : teams.length === 0 ? (
            <div className="text-center py-20 theme-faint text-sm">暂无队伍，来创建第一支吧</div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-3">
              {teams.map((team) => (
                <div key={team.id} className="theme-card rounded-2xl p-4 theme-hover cursor-pointer transition-all"
                  onClick={() => navigate(`/teams/${team.id}`)}>
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0 space-y-2 flex-1">
                      <div className="flex items-center gap-2">
                        <span className="font-medium theme-text">{team.teamName}</span>
                        <span className="text-[11px] px-2 py-0.5 rounded-full theme-tag">{team.memberCount} 人</span>
                      </div>
                      {team.description && <div className="text-sm theme-faint line-clamp-2">{team.description}</div>}
                      <div className="text-xs theme-faint">队长：{team.captainName}</div>
                      {team.memberNames.length > 0 && (
                        <div className="flex flex-wrap gap-1">
                          {team.memberNames.map((name, i) => (
                            <span key={i} className="rounded-full px-2 py-0.5 text-[10px] theme-tag">{name}</span>
                          ))}
                        </div>
                      )}
                    </div>
                    <button onClick={(e) => { e.stopPropagation(); void handleApply(team.id) }}
                      className="theme-button-secondary rounded-lg px-3 py-1.5 text-xs shrink-0">
                      申请加入
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}

          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 pt-2">
              <button onClick={() => setPage(Math.max(1, page - 1))} disabled={page === 1} className="theme-button-ghost text-xs disabled:opacity-30">&lt;</button>
              <span className="text-xs theme-faint">{page} / {totalPages}</span>
              <button onClick={() => setPage(Math.min(totalPages, page + 1))} disabled={page === totalPages} className="theme-button-ghost text-xs disabled:opacity-30">&gt;</button>
            </div>
          )}
        </main>
      </div>
    </div>
  )
}
