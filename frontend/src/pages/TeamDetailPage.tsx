import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Check, DoorOpen, Loader2, Shield, Trophy, UserMinus, Users, X } from 'lucide-react'
import {
  getIndTeamDetail, updateIndTeam, dissolveIndTeam, leaveIndTeam,
  removeIndTeamMember, transferIndTeamCaptain,
  getPendingRequests, approveJoinRequest, rejectJoinRequest,
} from '@/api/team'
import UserMenu from '@/components/UserMenu'
import ThemeToggle from '@/components/ThemeToggle'
import { confirm, toast } from '@/store/uiStore'
import type { IndTeamDetail, TeamJoinRequestItem } from '@/types'

export default function TeamDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const teamId = Number(id)

  const [team, setTeam] = useState<IndTeamDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [requests, setRequests] = useState<TeamJoinRequestItem[]>([])
  const [tab, setTab] = useState<'members' | 'contests' | 'requests' | 'settings'>('members')

  // 编辑
  const [editName, setEditName] = useState('')
  const [editDesc, setEditDesc] = useState('')

  useEffect(() => { void loadTeam() }, [teamId])

  const loadTeam = async () => {
    setLoading(true)
    try {
      const res = await getIndTeamDetail(teamId)
      if (res.code === 200) {
        setTeam(res.data)
        setEditName(res.data.teamName)
        setEditDesc(res.data.description || '')
        if (res.data.captain && res.data.pendingRequestCount > 0) void loadRequests()
      }
    } catch {}
    setLoading(false)
  }

  const loadRequests = async () => {
    try {
      const res = await getPendingRequests(teamId)
      if (res.code === 200) setRequests(res.data || [])
    } catch {}
  }

  const handleSave = async () => {
    if (!editName.trim()) { toast.warning('队伍名称不能为空'); return }
    try {
      await updateIndTeam(teamId, { teamName: editName.trim(), description: editDesc.trim() || undefined })
      toast.success('已更新')
      void loadTeam()
    } catch (e: any) { toast.error(e.message) }
  }

  const handleDissolve = async () => {
    if (!await confirm('确定解散队伍？已报名的比赛会同时取消。', { type: 'danger', confirmText: '解散队伍' })) return
    try {
      await dissolveIndTeam(teamId)
      toast.success('队伍已解散')
      navigate('/teams/mine')
    } catch (e: any) { toast.error(e.message) }
  }

  const handleLeave = async () => {
    if (!await confirm('确定退出队伍？', { type: 'warning', confirmText: '退出' })) return
    try {
      await leaveIndTeam(teamId)
      toast.success('已退出队伍')
      navigate('/teams/mine')
    } catch (e: any) { toast.error(e.message) }
  }

  const handleRemove = async (userId: number) => {
    if (!await confirm('确定移除该成员？', { type: 'warning', confirmText: '移除' })) return
    try {
      await removeIndTeamMember(teamId, userId)
      toast.success('已移除')
      void loadTeam()
    } catch (e: any) { toast.error(e.message) }
  }

  const handleTransfer = async (userId: number) => {
    if (!await confirm('确定转让队长？', { type: 'warning', confirmText: '转让' })) return
    try {
      await transferIndTeamCaptain(teamId, userId)
      toast.success('队长已转让')
      void loadTeam()
    } catch (e: any) { toast.error(e.message) }
  }

  const handleApprove = async (reqId: number) => {
    try {
      await approveJoinRequest(teamId, reqId)
      toast.success('已通过')
      void loadTeam()
      void loadRequests()
    } catch (e: any) { toast.error(e.message) }
  }

  const handleReject = async (reqId: number) => {
    try {
      await rejectJoinRequest(teamId, reqId)
      toast.success('已拒绝')
      void loadRequests()
    } catch (e: any) { toast.error(e.message) }
  }

  if (loading) return <div className="min-h-screen theme-bg flex items-center justify-center theme-faint"><Loader2 className="animate-spin mr-2" />加载中...</div>
  if (!team) return <div className="min-h-screen theme-bg flex items-center justify-center theme-faint">队伍不存在</div>

  const isCaptain = team.captain
  const tabs = [
    { key: 'members' as const, label: `成员 (${team.memberCount})` },
    { key: 'contests' as const, label: `参赛记录 (${team.contests.length})` },
    ...(isCaptain ? [
      { key: 'requests' as const, label: `申请${team.pendingRequestCount > 0 ? ` (${team.pendingRequestCount})` : ''}` },
      { key: 'settings' as const, label: '管理' },
    ] : []),
  ]

  return (
    <div className="min-h-screen theme-bg-gradient">
      <header className="theme-header px-6 py-3 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate('/teams/mine')} className="theme-button-ghost text-sm inline-flex items-center gap-1"><ArrowLeft size={15} />返回</button>
          <div>
            <div className="text-xs theme-faint">队伍详情</div>
            <h1 className="text-lg font-semibold theme-text">{team.teamName}</h1>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <ThemeToggle />
          <UserMenu />
        </div>
      </header>

      <div className="max-w-4xl mx-auto p-6 space-y-5">
        {/* 基本信息卡 */}
        <div className="theme-card rounded-2xl p-5 space-y-3">
          <div className="flex items-start justify-between gap-4">
            <div className="space-y-2 flex-1 min-w-0">
              <div className="flex items-center gap-2 flex-wrap">
                <span className="text-xl font-semibold theme-text">{team.teamName}</span>
                {isCaptain && <span className="text-[11px] px-2 py-0.5 rounded-full bg-amber-500/12 text-amber-300">我是队长</span>}
                {team.myRole === 'member' && <span className="text-[11px] px-2 py-0.5 rounded-full theme-tag">队员</span>}
              </div>
              {team.description && <p className="text-sm theme-faint">{team.description}</p>}
              <div className="flex items-center gap-4 text-sm theme-faint">
                <span>队长：{team.captainName}</span>
                <span>{team.memberCount} 人</span>
                <span>创建于 {new Date(team.createdAt).toLocaleDateString('zh-CN')}</span>
              </div>
            </div>
            {!isCaptain && team.myRole && (
              <button onClick={handleLeave} className="rounded-xl px-4 py-2 text-sm bg-[var(--danger)] text-white inline-flex items-center gap-1.5">
                <DoorOpen size={15} />退出队伍
              </button>
            )}
          </div>
        </div>

        {/* Tab */}
        <div className="flex gap-1 border-b theme-border">
          {tabs.map((t) => (
            <button key={t.key} onClick={() => { setTab(t.key); if (t.key === 'requests') void loadRequests() }}
              className={`px-4 py-2.5 text-sm border-b-2 transition ${tab === t.key ? 'border-[var(--accent)] theme-accent-text font-medium' : 'border-transparent theme-faint'}`}>
              {t.label}
            </button>
          ))}
        </div>

        {/* 成员 */}
        {tab === 'members' && (
          <div className="space-y-2">
            {team.members.map((m) => (
              <div key={m.userId} className="theme-card rounded-xl px-4 py-3 flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-medium theme-text">{m.username}</span>
                    <span className={`text-[11px] px-2 py-0.5 rounded-full ${m.role === 'captain' ? 'bg-amber-500/12 text-amber-300' : 'theme-tag'}`}>
                      {m.role === 'captain' ? '队长' : '队员'}
                    </span>
                  </div>
                  <div className="text-xs theme-faint mt-0.5">加入于 {new Date(m.joinedAt).toLocaleString('zh-CN')}</div>
                </div>
                {isCaptain && m.userId !== team.captainId && (
                  <div className="flex gap-2">
                    <button onClick={() => void handleTransfer(m.userId)} className="theme-button-secondary rounded-lg px-3 py-1.5 text-xs">转让队长</button>
                    <button onClick={() => void handleRemove(m.userId)} className="rounded-lg px-3 py-1.5 text-xs bg-[var(--danger)] text-white">移除</button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}

        {/* 参赛记录 */}
        {tab === 'contests' && (
          <div className="space-y-2">
            {team.contests.length === 0 ? (
              <div className="theme-card rounded-xl p-8 text-center text-sm theme-faint">尚未参加任何比赛</div>
            ) : team.contests.map((c) => (
              <div key={c.contestId} onClick={() => navigate(`/contests/${c.contestId}`)}
                className="theme-card rounded-xl px-4 py-3 theme-hover cursor-pointer flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <Trophy size={14} style={{ color: 'var(--warning)' }} />
                    <span className="font-medium theme-text">{c.contestTitle}</span>
                  </div>
                  <div className="text-xs theme-faint mt-0.5">{new Date(c.startTime).toLocaleString('zh-CN')}</div>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`text-[11px] px-2 py-0.5 rounded-full ${
                    c.contestStatus === 'running' ? 'bg-emerald-500/12 text-emerald-300' :
                    c.contestStatus === 'registering' ? 'bg-amber-500/12 text-amber-300' : 'theme-tag'
                  }`}>{c.contestStatus}</span>
                  <span className={`text-[11px] px-2 py-0.5 rounded-full ${c.registrationStatus === 'registered' ? 'bg-emerald-500/12 text-emerald-300' : 'theme-tag'}`}>
                    {c.registrationStatus === 'registered' ? '已报名' : c.registrationStatus}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 申请管理 */}
        {tab === 'requests' && isCaptain && (
          <div className="space-y-2">
            {requests.length === 0 ? (
              <div className="theme-card rounded-xl p-8 text-center text-sm theme-faint">暂无待处理的申请</div>
            ) : requests.map((r) => (
              <div key={r.id} className="theme-card rounded-xl px-4 py-3 flex items-center justify-between">
                <div>
                  <div className="font-medium theme-text">用户 #{r.userId}</div>
                  {r.message && <div className="text-sm theme-faint mt-0.5">留言：{r.message}</div>}
                  <div className="text-xs theme-hint mt-0.5">{new Date(r.createdAt).toLocaleString('zh-CN')}</div>
                </div>
                <div className="flex gap-2">
                  <button onClick={() => void handleApprove(r.id)} className="rounded-lg px-3 py-1.5 text-xs bg-emerald-600 text-white inline-flex items-center gap-1"><Check size={12} />通过</button>
                  <button onClick={() => void handleReject(r.id)} className="rounded-lg px-3 py-1.5 text-xs bg-[var(--danger)] text-white inline-flex items-center gap-1"><X size={12} />拒绝</button>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 管理设置 */}
        {tab === 'settings' && isCaptain && (
          <div className="space-y-4">
            <div className="theme-card rounded-2xl p-5 space-y-3">
              <div className="text-sm font-medium theme-text">队伍信息</div>
              <input value={editName} onChange={(e) => setEditName(e.target.value)} placeholder="队伍名称"
                className="w-full theme-input rounded-xl px-3 py-2 text-sm" />
              <textarea value={editDesc} onChange={(e) => setEditDesc(e.target.value)} placeholder="队伍描述"
                className="w-full theme-input rounded-xl px-3 py-2 text-sm resize-none" rows={2} maxLength={500} />
              <button onClick={handleSave} className="theme-button-blue rounded-xl px-4 py-2 text-sm">保存修改</button>
            </div>

            <div className="theme-card rounded-2xl p-5 space-y-3">
              <div className="text-sm font-medium text-[var(--danger)]">危险操作</div>
              <button onClick={handleDissolve} className="rounded-xl px-4 py-2 text-sm bg-[var(--danger)] text-white">解散队伍</button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
