import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Loader2, Shield, UserCheck, Users } from 'lucide-react'
import { getMyIndTeams } from '@/api/team'
import UserMenu from '@/components/UserMenu'
import ThemeToggle from '@/components/ThemeToggle'
import ContestSidebar from '@/components/ContestSidebar'
import type { IndTeamListItem } from '@/types'

export default function MyTeamsPage() {
  const navigate = useNavigate()
  const [teams, setTeams] = useState<IndTeamListItem[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => { void loadTeams() }, [])

  const loadTeams = async () => {
    setLoading(true)
    try {
      const res = await getMyIndTeams()
      if (res.code === 200) setTeams(res.data || [])
    } catch {}
    setLoading(false)
  }

  return (
    <div className="h-screen flex flex-col theme-bg-gradient overflow-hidden">
      <header className="theme-header px-6 py-3 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate('/')} className="theme-button-ghost text-sm">&larr; 返回题库</button>
          <h1 className="text-lg font-semibold flex items-center gap-2 theme-text">
            <UserCheck size={20} style={{ color: 'var(--accent)' }} />
            我的队伍
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
          {loading ? (
            <div className="flex items-center justify-center py-20 theme-faint"><Loader2 className="animate-spin mr-2" size={18} />加载中...</div>
          ) : teams.length === 0 ? (
            <div className="text-center py-20 space-y-3">
              <div className="text-5xl opacity-10">&#128101;</div>
              <p className="theme-faint text-sm">你还没有加入任何队伍</p>
              <button onClick={() => navigate('/teams')} className="theme-button-blue rounded-xl px-4 py-2 text-sm">前往队伍空间</button>
            </div>
          ) : (
            <div className="space-y-3">
              {teams.map((team) => (
                <div key={team.id}
                  onClick={() => navigate(`/teams/${team.id}`)}
                  className="theme-card rounded-2xl p-5 theme-hover cursor-pointer transition-all">
                  <div className="flex items-start justify-between gap-4">
                    <div className="space-y-2 min-w-0 flex-1">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="text-base font-semibold theme-text">{team.teamName}</span>
                        <span className="text-[11px] px-2 py-0.5 rounded-full theme-tag">{team.memberCount} 人</span>
                      </div>
                      {team.description && <div className="text-sm theme-faint line-clamp-2">{team.description}</div>}
                      <div className="flex items-center gap-3 text-sm theme-faint">
                        <span className="inline-flex items-center gap-1"><Shield size={13} />队长：{team.captainName}</span>
                        <span className="inline-flex items-center gap-1"><Users size={13} />{team.memberCount} 人</span>
                      </div>
                      {team.memberNames.length > 0 && (
                        <div className="flex flex-wrap gap-1">
                          {team.memberNames.map((name, i) => (
                            <span key={i} className="rounded-full px-2 py-0.5 text-[10px] theme-tag">{name}</span>
                          ))}
                        </div>
                      )}
                    </div>
                    <span className="text-xs theme-faint shrink-0">查看详情 &rarr;</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </main>
      </div>
    </div>
  )
}
